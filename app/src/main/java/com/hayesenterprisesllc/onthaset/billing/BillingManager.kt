package com.hayesenterprisesllc.onthaset.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.hayesenterprisesllc.onthaset.data.Http
import com.hayesenterprisesllc.onthaset.data.Products
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/** Simplified product info shown in the UI (StoreKit `Product` replacement). */
data class StoreProduct(val id: String, val displayName: String, val displayPrice: String, val details: ProductDetails)

sealed class PurchaseOutcome {
    data object Success : PurchaseOutcome()
    data object Cancelled : PurchaseOutcome()
    data object Pending : PurchaseOutcome()
    data class Failed(val message: String) : PurchaseOutcome()
}

/**
 * Google Play Billing port of StoreKitManager.swift.
 * Create the same two product IDs in Play Console:
 *  - com.onthaset.singlepost (in-app, consumable, $0.99)
 *  - com.hayesenterprisellc.onthaset.membership.monthly (subscription, $2.99/month)
 */
object BillingManager : PurchasesUpdatedListener {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var client: BillingClient

    private val _products = MutableStateFlow<List<StoreProduct>>(emptyList())
    val products: StateFlow<List<StoreProduct>> = _products

    private val _purchasedProductIDs = MutableStateFlow<Set<String>>(emptySet())
    val purchasedProductIDs: StateFlow<Set<String>> = _purchasedProductIDs

    private val _hasActiveSubscription = MutableStateFlow(false)
    val hasActiveSubscription: StateFlow<Boolean> = _hasActiveSubscription

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var pendingPurchase: CompletableDeferred<PurchaseOutcome>? = null

    fun init(context: Context) {
        client = BillingClient.newBuilder(context.applicationContext)
            .setListener(this)
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .build()
        scope.launch {
            if (ensureConnected()) {
                loadProducts()
                updatePurchasedProducts()
            }
        }
    }

    private suspend fun ensureConnected(): Boolean {
        if (client.isReady) return true
        return suspendCoroutine { cont ->
            var resumed = false
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (!resumed) { resumed = true; cont.resume(result.responseCode == BillingClient.BillingResponseCode.OK) }
                }
                override fun onBillingServiceDisconnected() {
                    if (!resumed) { resumed = true; cont.resume(false) }
                }
            })
        }
    }

    suspend fun loadProducts() {
        _isLoading.value = true
        try {
            if (!ensureConnected()) return
            val loaded = mutableListOf<StoreProduct>()

            val inapp = client.queryProductDetails(
                QueryProductDetailsParams.newBuilder().setProductList(listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(Products.SINGLE_POST).setProductType(BillingClient.ProductType.INAPP).build()
                )).build()
            )
            inapp.productDetailsList?.forEach { d ->
                loaded += StoreProduct(d.productId, d.name, d.oneTimePurchaseOfferDetails?.formattedPrice ?: "$0.99", d)
            }

            val subs = client.queryProductDetails(
                QueryProductDetailsParams.newBuilder().setProductList(listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(Products.MONTHLY_SUBSCRIPTION).setProductType(BillingClient.ProductType.SUBS).build()
                )).build()
            )
            subs.productDetailsList?.forEach { d ->
                val price = d.subscriptionOfferDetails?.firstOrNull()
                    ?.pricingPhases?.pricingPhaseList?.lastOrNull()?.formattedPrice ?: "$2.99"
                loaded += StoreProduct(d.productId, d.name, price, d)
            }

            _products.value = loaded
            Http.log("📦 Loaded ${loaded.size} Play products")
        } catch (e: Exception) {
            Http.log("❌ Error loading products: $e")
        } finally {
            _isLoading.value = false
        }
    }

    val singlePostProduct: StoreProduct? get() = _products.value.firstOrNull { it.id == Products.SINGLE_POST }
    val subscriptionProduct: StoreProduct? get() = _products.value.firstOrNull { it.id == Products.MONTHLY_SUBSCRIPTION }

    suspend fun purchase(activity: Activity, product: StoreProduct): PurchaseOutcome {
        if (!ensureConnected()) return PurchaseOutcome.Failed("Google Play is unavailable")
        _isLoading.value = true
        val paramsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(product.details)
        product.details.subscriptionOfferDetails?.firstOrNull()?.offerToken?.let { paramsBuilder.setOfferToken(it) }
        val flowParams = BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(paramsBuilder.build())).build()

        val deferred = CompletableDeferred<PurchaseOutcome>()
        pendingPurchase = deferred
        val launch = client.launchBillingFlow(activity, flowParams)
        if (launch.responseCode != BillingClient.BillingResponseCode.OK) {
            pendingPurchase = null
            _isLoading.value = false
            return PurchaseOutcome.Failed(launch.debugMessage.ifEmpty { "Purchase failed" })
        }
        val result = deferred.await()
        _isLoading.value = false
        return result
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        val deferred = pendingPurchase
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> scope.launch {
                var outcome: PurchaseOutcome = PurchaseOutcome.Success
                purchases.orEmpty().forEach { p ->
                    if (p.purchaseState == Purchase.PurchaseState.PENDING) outcome = PurchaseOutcome.Pending
                    else handlePurchase(p)
                }
                updatePurchasedProducts()
                deferred?.complete(outcome)
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> deferred?.complete(PurchaseOutcome.Cancelled)
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> scope.launch {
                updatePurchasedProducts(); deferred?.complete(PurchaseOutcome.Success)
            }
            else -> deferred?.complete(PurchaseOutcome.Failed(result.debugMessage.ifEmpty { "Purchase failed (${result.responseCode})" }))
        }
        pendingPurchase = null
    }

    private suspend fun handlePurchase(p: Purchase) {
        if (p.purchaseState != Purchase.PurchaseState.PURCHASED) return
        if (p.products.contains(Products.SINGLE_POST)) {
            // Consumable — consume so it can be bought again.
            client.consumePurchase(ConsumeParams.newBuilder().setPurchaseToken(p.purchaseToken).build())
            _purchasedProductIDs.value = _purchasedProductIDs.value + Products.SINGLE_POST
        } else if (!p.isAcknowledged) {
            client.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(p.purchaseToken).build())
        }
    }

    suspend fun updatePurchasedProducts() {
        if (!ensureConnected()) return
        val ids = mutableSetOf<String>()
        var hasSub = false
        runCatching {
            val subs = client.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build())
            subs.purchasesList.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }.forEach { p ->
                if (!p.isAcknowledged) handlePurchase(p)
                ids += p.products
                if (p.products.contains(Products.MONTHLY_SUBSCRIPTION)) hasSub = true
            }
            val inapp = client.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build())
            inapp.purchasesList.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }.forEach { p -> handlePurchase(p) }
        }
        // Keep a just-bought single post available for this session (consumables vanish from queries).
        if (_purchasedProductIDs.value.contains(Products.SINGLE_POST)) ids += Products.SINGLE_POST
        _purchasedProductIDs.value = ids
        _hasActiveSubscription.value = hasSub
        Http.log("📅 Has subscription: $hasSub")
    }

    /** Called after a single-post purchase has been used. */
    fun consumeSinglePostCredit() {
        _purchasedProductIDs.value = _purchasedProductIDs.value - Products.SINGLE_POST
    }

    suspend fun restorePurchases() {
        _isLoading.value = true
        updatePurchasedProducts()
        _isLoading.value = false
    }
}
