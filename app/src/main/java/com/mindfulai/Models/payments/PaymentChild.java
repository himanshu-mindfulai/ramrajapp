package com.mindfulai.Models.payments;

import java.util.ArrayList;

public class PaymentChild {
    private long page;
    private long limit;
    private int totalCount;
    private ArrayList<PaymentRecords> records;

    public long getPage() { return page; }
    public void setPage(long value) { this.page = value; }

    public long getLimit() { return limit; }
    public void setLimit(long value) { this.limit = value; }

    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int value) { this.totalCount = value; }

    public ArrayList<PaymentRecords> getRecords() {
        return records;
    }

    public void setRecords(ArrayList<PaymentRecords> records) {
        this.records = records;
    }
}
