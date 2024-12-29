package com.mp.travel_app.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mp.travel_app.Domain.Ticket;
import com.mp.travel_app.Domain.Tour;
import com.mp.travel_app.Domain.Users;
import com.mp.travel_app.R;
import com.mp.travel_app.Utils.Common;
import com.mp.travel_app.Utils.Stripe;
import com.mp.travel_app.Utils.TourCartManager;
import com.mp.travel_app.databinding.ActivityTicketBinding;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

public class TicketActivity extends BaseActivity {
    private static final String TAG = "TicketActivity";
    ActivityTicketBinding binding;
    private Tour object;
    private Ticket ticket;
    private PaymentSheet paymentSheet;
    private TourCartManager cartManager;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTicketBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cartManager = new TourCartManager(this);
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        getIntentExtra();
        getExistedTicket();

        binding.btnPay.setOnClickListener(v -> handlePay());
        binding.backBtn.setOnClickListener(v -> finish());
    }

    private void getExistedTicket() {
        ticket = (Ticket) getIntent().getSerializableExtra("ticket");

        if (ticket != null) {
            object = ticket.getTour();
        } else {
            Log.d(TAG, "No ticket found");
        }

        if (ticket == null && object == null) {
            Log.e(TAG, "No ticket or tour found");
            finish();
        } else {
            setVariable();
        }
    }

    private void handlePay() {
        binding.btnPay.setEnabled(false);
        Common.getCurrentUser(this, new Common.GetUserCallback() {
            @Override
            public void onSuccess(Users user) {
                if (ticket != null) {
                    if ("Unpaid".equals(ticket.getStatus())) {
                        Log.d(TAG, "Using existing payment for ticket");
                        binding.btnPay.setEnabled(true);
                        Stripe.presentExistingPaymentSheet(TicketActivity.this, paymentSheet, ticket.getStripePaymentId(), user.getStripeCustomerId(), new Stripe.PaymentCallback() {
                            @Override
                            public void onSuccess(String result) {
                                Log.d(TAG, "Payment ID: " + result);
                            }

                            @Override
                            public void onError(String error) {
                                Log.e(TAG, "Error payment: " + error);
                            }
                        });
                    } else {
                        Log.d(TAG, "Ticket is already paid.");
                        binding.btnPay.setEnabled(true);
                    }
                } else {
                    createTicket(user);
                    cartManager.removeTour(object.getId());

                    if (user.getStripeCustomerId() == null) {
                        Stripe.createCustomer(user.getUsername(), user.getEmail(), new Stripe.Callback() {
                            @Override
                            public void onSuccess(String result) {
                                user.setStripeCustomerId(result);

                                database.getReference("Users")
                                        .child(user.getId())
                                        .setValue(user)
                                        .addOnSuccessListener(aVoid -> {
                                            createNewPayment(user.getStripeCustomerId());
                                            binding.btnPay.setEnabled(true);
                                        })
                                        .addOnFailureListener(e -> Log.e(TAG, "Error updating user: ", e));
                            }

                            @Override
                            public void onError(String error) {
                                Log.e(TAG, "Error creating customer: " + error);
                                binding.btnPay.setEnabled(true);
                            }
                        });
                    } else {
                        createNewPayment(user.getStripeCustomerId());
                        binding.btnPay.setEnabled(true);
                    }
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "Error fetching user: " + errorMessage);
                binding.btnPay.setEnabled(true);
            }
        });
    }

    private void createNewPayment(String customerId) {
        Stripe.handlePayment(this, paymentSheet, customerId, (int) object.getPrice(), new Stripe.PaymentCallback() {
            @Override
            public void onSuccess(String paymentId) {
                Log.d(TAG, "Payment ID: " + paymentId);
                if (ticket != null) {
                    ticket.setStripePaymentId(paymentId);
                    database.getReference("Ticket")
                            .child(ticket.getId())
                            .setValue(ticket)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Ticket updated with payment ID"))
                            .addOnFailureListener(e -> Log.e(TAG, "Error updating ticket: ", e));
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error creating payment: " + error);
            }
        });
    }

    private void createTicket(Users currentUser) {
        ticket = new Ticket();
        ticket.setTour(object);
        ticket.setCustomer(currentUser);
        ticket.setStatus("Unpaid");
        ticket.setCreatedAt(Common.getCurrentDateTime());

        String ticketId = database.getReference("Ticket").push().getKey();

        if (ticketId != null) {
            ticket.setId(ticketId);
            database.getReference("Ticket")
                    .child(ticketId)
                    .setValue(ticket)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Ticket created successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error creating ticket: ", e));
        }
    }

    private void onPaymentSheetResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Log.d(TAG, "Payment canceled");
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            Log.e(TAG, "Payment failed: ", ((PaymentSheetResult.Failed) paymentSheetResult).getError());
        } else if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Log.d(TAG, "Payment completed successfully");
            if (ticket != null) {
                ticket.setStatus("Paid");
                database.getReference("Ticket")
                        .child(ticket.getId())
                        .setValue(ticket)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Ticket updated to 'Paid'"))
                        .addOnFailureListener(e -> Log.e(TAG, "Error updating ticket: ", e));
            }
            bottomNavigationView.setSelectedItemId(R.id.menuCart);
        }
    }

    @SuppressLint("SetTextI18n")
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

        if (ticket != null) {
            binding.txtStatus.setText(ticket.getStatus());
        } else {
            binding.txtStatus.setText("Ready");
        }

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
        object = (Tour) getIntent().getSerializableExtra("item");
    }
}