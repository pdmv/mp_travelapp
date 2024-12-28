package com.mp.travel_app.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.mp.travel_app.Domain.Tour;
import com.mp.travel_app.Domain.Users;
import com.mp.travel_app.Utils.Common;
import com.mp.travel_app.Utils.Stripe;
import com.mp.travel_app.databinding.ActivityTicketBinding;
import com.stripe.android.paymentsheet.PaymentSheet;

public class TicketActivity extends BaseActivity {
    private static final String TAG = "TicketActivity";
    ActivityTicketBinding binding;
    private Tour object;
    private PaymentSheet paymentSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTicketBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        paymentSheet = new PaymentSheet(this, Stripe::onPaymentSheetResult);

        binding.btnPay.setOnClickListener(v -> handlePay());

        getIntentExtra();
        setVariable();
    }

    private void handlePay() {
        binding.btnPay.setEnabled(false);
        Common.getCurrentUser(this, new Common.GetUserCallback() {
            @Override
            public void onSuccess(Users user) {
                if (user.getStripeCustomerId() == null) {
                    Stripe.createCustomer(user.getUsername(), user.getEmail(), new Stripe.Callback() {
                        @Override
                        public void onSuccess(String result) {
                            user.setStripeCustomerId(result);

                            database.getReference("Users")
                                    .child(user.getId())
                                    .setValue(user)
                                    .addOnSuccessListener(aVoid -> {
                                        pay(user.getStripeCustomerId());
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "onFailure: ", e);
                                    });

                            binding.btnPay.setEnabled(true);
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "Error creating customer: " + error);
                            binding.btnPay.setEnabled(true);
                        }
                    });
                } else {
                    pay(user.getStripeCustomerId());
                    binding.btnPay.setEnabled(true);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "onFailure: " + errorMessage);
                binding.btnPay.setEnabled(true);
            }
        });
    }

    private void pay(String customerId) {
        Stripe.handlePayment(this, paymentSheet, customerId, (int) object.getPrice());
    }

    private void setVariable() {
        Glide.with(TicketActivity.this)
                .load(object.getImagePath())
                .into(binding.pic);

        Common.getFileFromFirebase(object.getTourGuide().getAvatar(), new Common.OnGetFileListener() {
            @Override
            public void onUploadSuccess(String downloadUrl) {
                Glide.with(TicketActivity.this)
                        .load(downloadUrl)
                        .into(binding.tourGuidePic);
            }

            @Override
            public void onUploadFailed(String errorMessage) {

            }
        });

        binding.backBtn.setOnClickListener(v -> finish());
        binding.titleTxt.setText(object.getTitle());
        binding.durationTxt.setText(object.getDuration());
        binding.tourGuideTxt.setText(object.getTourGuide().getFullname());
        binding.tourGuideNameTxt.setText(object.getTourGuide().getFullname());
        binding.timeTxt.setText(object.getTimeTour());
        binding.guestTxt.setText("Update soon");

        binding.messageBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("sms:" + object.getTourGuide().getPhoneNumber()));
            intent.putExtra("sms_body", "Hello, I want to ask about the tour");
            startActivity(intent);
        });

        binding.callBtn.setOnClickListener(v -> {
            String phone = object.getTourGuide().getPhoneNumber();
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
            startActivity(intent);
        });
    }

    private void getIntentExtra() {
        object = (Tour) getIntent().getSerializableExtra("object");
    }
}