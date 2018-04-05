function AdjustEvent(eventToken) {
    this.eventToken = eventToken;

    this.revenue = null;
    this.currency = null;
    this.callbackParameters = [];
    this.partnerParameters = [];
    this.orderId = null;

    this.setRevenue = function(revenue, currency) {
        this.revenue = revenue;
        this.currency = currency;
    }

    this.addCallbackParameter = function(key, value) {
        this.callbackParameters.push(key);
        this.callbackParameters.push(value);
    }

    this.addPartnerParameter = function(key, value) {
        this.partnerParameters.push(key);
        this.partnerParameters.push(value);
    }

    this.setOrderId = function(orderId) {
        this.orderId = orderId;
    }
}
