package com.mp.travel_app.Utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Handler;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import kotlin.text.Charsets;

public class Stripe {
    private static final String TAG = "Stripe";
    public static final String BASE_URL = "https://stripeserver-05gr.onrender.com";

    public static void handlePayment(Context context, PaymentSheet paymentSheet, String customerId, Integer amount, PaymentCallback paymentCallback) {
        Log.d(TAG, "Amount: " + amount);

        createEphemeralKey(customerId, new Callback() {
            @Override
            public void onSuccess(String ephemeralKey) {
                createPaymentIntent(context, customerId, amount, new Callback() {
                    @Override
                    public void onSuccess(String paymentIntent) {
                        paymentSheet.presentWithPaymentIntent(paymentIntent);
                        paymentCallback.onSuccess(paymentIntent);
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error creating PaymentIntent: " + error);
                        paymentCallback.onError(error);
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error creating Ephemeral Key: " + error);
                paymentCallback.onError(error);
            }
        });
    }

    public static  void presentExistingPaymentSheet(Context context, PaymentSheet paymentSheet, String paymentId, String customerId, PaymentCallback paymentCallback) {
        JSONObject body = new JSONObject();
        try {
            body.put("customerId", customerId);
        } catch (JSONException e) {
            paymentCallback.onError("JSON error: " + e.getMessage());
            return;
        }

        Fuel.INSTANCE.post(BASE_URL + "/ephemeral-keys", null)
                .body(body.toString(), Charsets.UTF_8)
                .header("Content-Type", "application/json")
                .responseString(new Handler<>() {
                    @Override
                    public void success(String s) {
                        try {
                            JSONObject result = new JSONObject(s);
                            String ephemeralKey = result.getString("ephemeralKey");
                            String publishableKey = result.getString("publishableKey");

                            PaymentConfiguration.init(context, publishableKey);

                            PaymentSheet.CustomerConfiguration customerConfig =
                                    createCustomerConfig(customerId, ephemeralKey);
                            presentPaymentSheet(paymentSheet, customerConfig, paymentId);

                            paymentCallback.onSuccess(paymentId);
                        } catch (JSONException e) {
                            paymentCallback.onError("JSON error: " + e.getMessage());
                        }
                    }

                    @Override
                    public void failure(@NonNull FuelError fuelError) {
                        paymentCallback.onError("Failed to create ephemeral key: " + fuelError.getMessage());
                    }
                });
    }

    public static void createCustomer(String name, String email, Callback callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("name", name);
            body.put("email", email);
        } catch (JSONException e) {
            callback.onError("JSON error: " + e.getMessage());
            return;
        }

        Fuel.INSTANCE.post(BASE_URL + "/customers", null)
                .body(body.toString(), Charsets.UTF_8)
                .header("Content-Type", "application/json")
                .responseString(new Handler<>() {
                    @Override
                    public void success(String s) {
                        try {
                            JSONObject result = new JSONObject(s);
                            callback.onSuccess(result.getString("customerId"));
                        } catch (JSONException e) {
                            callback.onError("JSON error: " + e.getMessage());
                        }
                    }

                    @Override
                    public void failure(@NonNull FuelError fuelError) {
                        callback.onError("Failed to create customer: " + fuelError.getMessage());
                    }
                });
    }

    public static void createEphemeralKey(String customerId, Callback callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("customerId", customerId);
        } catch (JSONException e) {
            callback.onError("JSON error: " + e.getMessage());
            return;
        }

        Fuel.INSTANCE.post(BASE_URL + "/ephemeral-keys", null)
                .body(body.toString(), Charsets.UTF_8)
                .header("Content-Type", "application/json")
                .responseString(new Handler<>() {
                    @Override
                    public void success(String s) {
                        try {
                            JSONObject result = new JSONObject(s);
                            callback.onSuccess(result.getString("ephemeralKey"));
                        } catch (JSONException e) {
                            callback.onError("JSON error: " + e.getMessage());
                        }
                    }

                    @Override
                    public void failure(@NonNull FuelError fuelError) {
                        callback.onError("Failed to create ephemeral key: " + fuelError.getMessage());
                    }
                });
    }

    public static void createPaymentIntent(Context context, String customerId, Integer amount, Callback callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("customerId", customerId);
            body.put("amount", amount + "00");
            body.put("currency", "usd");
        } catch (JSONException e) {
            callback.onError("JSON error: " + e.getMessage());
            return;
        }

        Log.d(TAG, body.toString());

        Fuel.INSTANCE.post(BASE_URL + "/payment-intents", null)
                .body(body.toString(), Charsets.UTF_8)
                .header("Content-Type", "application/json")
                .responseString(new Handler<>() {
                    @Override
                    public void success(String s) {
                        try {
                            JSONObject result = new JSONObject(s);
                            PaymentConfiguration.init(context, result.getString("publishableKey"));
                            callback.onSuccess(result.getString("paymentIntent"));
                        } catch (JSONException e) {
                            callback.onError("JSON error: " + e.getMessage());
                        }
                    }

                    @Override
                    public void failure(@NonNull FuelError fuelError) {
                        callback.onError("Failed to create PaymentIntent: " + fuelError.getMessage());
                    }
                });
    }

    public static PaymentSheet.CustomerConfiguration createCustomerConfig(String customerId, String ephemeralKey) {
        return new PaymentSheet.CustomerConfiguration(customerId, ephemeralKey);
    }

    public static void presentPaymentSheet(PaymentSheet paymentSheet, PaymentSheet.CustomerConfiguration customerConfiguration, String paymentClientSecret) {
        final PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder("Travel App, Inc.")
                .customer(customerConfiguration)
                .allowsDelayedPaymentMethods(true)
                .build();

        paymentSheet.presentWithPaymentIntent(paymentClientSecret, configuration);
    }

    public static void onPaymentSheetResult(final PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Log.d(TAG, "Canceled");
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            Log.e(TAG, "Got error: ", ((PaymentSheetResult.Failed) paymentSheetResult).getError());
        } else if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Log.d(TAG, "Payment completed successfully");
        }
    }

    public interface Callback {
        void onSuccess(String result);

        void onError(String error);
    }

    public interface  PaymentCallback {
        void onSuccess(String paymentId);
        void onError(String error);
    }
}
