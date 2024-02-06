package org.killbill.billing.plugin.helloworld;

import org.killbill.billing.notification.plugin.api.ExtBusEvent;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillEventDispatcher;
import org.killbill.billing.plugin.api.PluginTenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloWorldListener implements OSGIKillbillEventDispatcher.OSGIKillbillEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(HelloWorldListener.class);

    private final OSGIKillbillAPI osgiKillbillAPI;

    public HelloWorldListener(final OSGIKillbillAPI killbillAPI) {
        this.osgiKillbillAPI = killbillAPI;
    }

    @Override
    public void handleKillbillEvent(final ExtBusEvent killbillEvent) {
        logger.info("Received event {} for object id {} of type {}",
                    killbillEvent.getEventType(),
                    killbillEvent.getObjectId(),
                    killbillEvent.getObjectType());

        final PluginTenantContext context = new PluginTenantContext(killbillEvent.getAccountId(), killbillEvent.getTenantId());
        switch (killbillEvent.getEventType()) {
            // TODO: Add case for INVOICE_CREATION to handle new subscription and invoice generation
            // TODO: Add case for OVERDUE_INVOICE to handle overdue status
            // TODO: Add cases for PAYMENT_FAILED and PAYMENT_SUCCESS to handle payment retries

            // Example for handling new subscriptions (INVOICE_CREATION)
            case INVOICE_CREATION:
                handleInvoiceCreation(killbillEvent, context);
                break;
            
            // Example for handling overdue invoices
            case OVERDUE_INVOICE:
                handleOverdueInvoice(killbillEvent, context);
                break;

            // Example for handling payment retries
            case PAYMENT_FAILED:
                handlePaymentFailed(killbillEvent, context);
                break;
            case PAYMENT_SUCCESS:
                handlePaymentSuccess(killbillEvent, context);
                break;

            // Existing cases...
            case ACCOUNT_CREATION:
            case ACCOUNT_CHANGE:
                handleAccountChange(killbillEvent, context);
                break;

            default:
                break;
        }
    }

    private void handleInvoiceCreation(final ExtBusEvent event, final PluginTenantContext context) {
        // Extract information from the event necessary for the webhook
        UUID invoiceId = event.getObjectId(); // Assuming the objectId here is the invoiceId
    
        // Format the data as required by Xano's webhook endpoint
        Map<String, Object> payload = new HashMap<>();
        payload.put("invoiceId", invoiceId.toString());
        // Add any other necessary information to the payload
    
        // Serialize payload to JSON (using your preferred JSON library)
        String jsonPayload = serializeToJson(payload);
    
        // Send the webhook
        sendWebhook(jsonPayload, "https://x8ki-letl-twmt.n7.xano.io/api:yk7F8i6p/killbill_webhook"); // Replace with your actual Xano webhook URL
    }
    
       
    

    private void handleOverdueInvoice(final ExtBusEvent event, final PluginTenantContext context) {
        UUID invoiceId = event.getObjectId(); // Assuming the objectId here is the invoiceId
    
        // Format the data as required by Xano's webhook endpoint
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", "OVERDUE_INVOICE");
        payload.put("invoiceId", invoiceId.toString());
        // Add any other necessary information to the payload
    
        // Serialize payload to JSON and send the webhook
        sendWebhook(payload);
    }
    
    

    private void handleAccountChange(final ExtBusEvent event, final PluginTenantContext context) {
        try {
            final Account account = osgiKillbillAPI.getAccountUserApi().getAccountById(event.getAccountId(), context);
            logger.info("Account information: " + account);
        } catch (final AccountApiException e) {
            logger.warn("Unable to find account", e);
        }
    }
    


    // A utility method to send the webhook payload
    private void sendWebhook(final String payload, final String webhookUrl) {
        // Use a HTTP client to send the webhook
        // You might need to set additional headers, authentication, etc.
        // This is pseudocode and won't run as is.
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            // Check the response status code
            if (response.statusCode() == 200) {
                logger.info("Webhook sent successfully");
            } else {
                // Handle non-200 responses appropriately
                logger.error("Error sending webhook. Status code: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            // Handle exceptions
            logger.error("Error sending webhook", e);
        }
    }

    // Serialize to JSON (using your preferred JSON library)
    private String serializeToJson(Map<String, Object> payload) {
        // This is pseudocode and won't run as is.
        // You would use a library like Jackson or Gson to convert the map to a JSON string.
        // For example, with Jackson:
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing payload to JSON", e);
            return "{}";
        }
    }
}

