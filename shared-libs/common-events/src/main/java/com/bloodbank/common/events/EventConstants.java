package com.bloodbank.common.events;

public final class EventConstants {

    private EventConstants() {}

    public static final String EXCHANGE_NAME = "bloodbank.events";

    public static final String DONATION_COMPLETED = "donation.completed";
    public static final String CAMP_COMPLETED = "camp.completed";
    public static final String TEST_RESULT_AVAILABLE = "test.result.available";
    public static final String UNIT_RELEASED = "unit.released";
    public static final String BLOOD_STOCK_UPDATED = "blood.stock.updated";
    public static final String STOCK_CRITICAL = "stock.critical";
    public static final String UNIT_EXPIRING = "unit.expiring";
    public static final String BLOOD_REQUEST_CREATED = "blood.request.created";
    public static final String BLOOD_REQUEST_MATCHED = "blood.request.matched";
    public static final String EMERGENCY_REQUEST = "emergency.request";
    public static final String TRANSFUSION_COMPLETED = "transfusion.completed";
    public static final String TRANSFUSION_REACTION = "transfusion.reaction";
    public static final String INVOICE_GENERATED = "invoice.generated";
    public static final String RECALL_INITIATED = "recall.initiated";
}
