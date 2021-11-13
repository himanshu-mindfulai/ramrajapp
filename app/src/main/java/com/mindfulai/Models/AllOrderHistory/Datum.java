package com.mindfulai.Models.AllOrderHistory;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mindfulai.Models.CartInformation.Coupon;
import com.mindfulai.Models.CustomOrderData;
import com.mindfulai.Models.UserDataAddress;

import java.util.ArrayList;
import java.util.List;

public class Datum implements Parcelable {

    @SerializedName("deliveryCharge")
    @Expose
    private double deliveryCharge;
    @SerializedName("_id")
    @Expose
    private String id;
    @SerializedName("placed_by")
    @Expose
    private String placedBy;
    @SerializedName("order_id")
    @Expose
    private String orderId;
    @SerializedName("products")
    @Expose
    private List<Product> products;
    @SerializedName("amount")
    @Expose
    private float amount;
    @SerializedName("address")
    @Expose
    private UserDataAddress address;
    @SerializedName("deliverySlot")
    @Expose
    private String deliverySlot;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("order_date")
    @Expose
    private String orderDate;
    @SerializedName("isDelivered")
    @Expose
    private boolean isDelivered;
    @SerializedName("isCancelled")
    @Expose
    private boolean isCancelled;
    @SerializedName("paymentMethod")
    @Expose
    private String paymentMethod;
    @SerializedName("paymentStatus")
    @Expose
    private String paymentStatus;
    @SerializedName("deliveryType")
    @Expose
    private String orderType;
    @SerializedName("paidFromWallet")
    @Expose
    private float paidFromWallet;
    @SerializedName("carryBagCharge")
    private float carryBagCharge;
    @SerializedName("coupon")
    private Coupon coupon;
    @SerializedName("items")
    private List<CustomOrderData> items;

    @SerializedName("deliveryDetails")
    private String deliveryDetails;
    @SerializedName("invoiceNumber")
    private String invoiceNumber;
    @SerializedName("adminNote")
    private String adminNote;

    public Datum() {
    }


    protected Datum(Parcel in) {
        deliveryCharge = in.readDouble();
        id = in.readString();
        placedBy = in.readString();
        orderId = in.readString();
        products = in.createTypedArrayList(Product.CREATOR);
        amount = in.readFloat();
        address = in.readParcelable(UserDataAddress.class.getClassLoader());
        deliverySlot = in.readString();
        status = in.readString();
        orderDate = in.readString();
        isDelivered = in.readByte() != 0;
        isCancelled = in.readByte() != 0;
        paymentMethod = in.readString();
        orderType = in.readString();
        paidFromWallet = in.readFloat();
        carryBagCharge = in.readFloat();
        coupon = in.readParcelable(Coupon.class.getClassLoader());
        items = in.createTypedArrayList(CustomOrderData.CREATOR);
        deliveryDetails = in.readString();
        invoiceNumber = in.readString();
        adminNote = in.readString();
        paymentStatus = in.readString();
    }


    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public static final Creator<Datum> CREATOR = new Creator<Datum>() {
        @Override
        public Datum createFromParcel(Parcel in) {
            return new Datum(in);
        }

        @Override
        public Datum[] newArray(int size) {
            return new Datum[size];
        }
    };

    public String getAdminNote() {
        return adminNote;
    }

    public String getDeliveryDetails() {
        return deliveryDetails;
    }

    public List<CustomOrderData> getItems() {
        return items;
    }

    public void setItems(List<CustomOrderData> items) {
        this.items = items;
    }

    public float getCarryBagCharge() {
        return carryBagCharge;
    }

    public void setCarryBagCharge(float carryBagCharge) {
        this.carryBagCharge = carryBagCharge;
    }

    public float getPaidFromWallet() {
        return paidFromWallet;
    }

    public void setPaidFromWallet(float paidFromWallet) {
        this.paidFromWallet = paidFromWallet;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public UserDataAddress getAddress() {
        return address;
    }

    public void setAddress(UserDataAddress address) {
        this.address = address;
    }

    public String getDeliverySlot() {
        return deliverySlot;
    }

    public void setDeliverySlot(String deliverySlot) {
        this.deliverySlot = deliverySlot;
    }

    public Boolean getIsDelivered() {
        return isDelivered;
    }

    public void setIsDelivered(Boolean isDelivered) {
        this.isDelivered = isDelivered;
    }

    public Boolean getIsCancelled() {
        return isCancelled;
    }

    public void setIsCancelled(Boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlacedBy() {
        return placedBy;
    }

    public void setPlacedBy(String placedBy) {
        this.placedBy = placedBy;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public double getDeliveryCharge() {
        return deliveryCharge;
    }

    public void setDeliveryCharge(double deliveryCharge) {
        this.deliveryCharge = deliveryCharge;
    }


    public Coupon getCoupon() {
        return coupon;
    }

    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(deliveryCharge);
        dest.writeString(id);
        dest.writeString(placedBy);
        dest.writeString(orderId);
        dest.writeTypedList(products);
        dest.writeFloat(amount);
        dest.writeParcelable(address, flags);
        dest.writeString(deliverySlot);
        dest.writeString(status);
        dest.writeString(orderDate);
        dest.writeByte((byte) (isDelivered ? 1 : 0));
        dest.writeByte((byte) (isCancelled ? 1 : 0));
        dest.writeString(paymentMethod);
        dest.writeString(orderType);
        dest.writeFloat(paidFromWallet);
        dest.writeFloat(carryBagCharge);
        dest.writeParcelable(coupon, flags);
        dest.writeTypedList(items);
        dest.writeString(deliveryDetails);
        dest.writeString(invoiceNumber);
        dest.writeString(adminNote);
        dest.writeString(paymentStatus);
    }
}
