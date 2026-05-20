package lk.ijse.theserenitymentalhealththerapycenter.util;

import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class MailClientUtil {

    /**
     * Aggregates recipient email addresses, sets them to system clipboard,
     * and triggers default web browser straight to Gmail's composition window using safe BCC fields.
     */
    public static void launchGmailBccComposer(List<String> rawEmailList, String customSubject, String customMessageBody) {
        if (rawEmailList == null || rawEmailList.isEmpty()) {
            AlertUtil.showWarning("Communication Error", "No Recipients Selected", "The target list contains zero active client records.");
            return;
        }

        // Sanitize and isolate individual unique email addresses
        String bccAggregatorString = rawEmailList.stream()
                .filter(email -> email != null && email.contains("@"))
                .distinct()
                .collect(Collectors.joining(","));

        if (bccAggregatorString.isEmpty()) {
            AlertUtil.showWarning("Validation Error", "Invalid Destinations", "None of the selected records contain a valid '@' symbol schema mapping.");
            return;
        }

        try {
            // Backup to clipboard for seamless user fallback assistance
            StringSelection clipboardPayload = new StringSelection(bccAggregatorString);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(clipboardPayload, null);

            // Encode text inputs cleanly into formal URI query strings
            String encodedSubject = URLEncoder.encode(customSubject, StandardCharsets.UTF_8);
            String encodedBody = URLEncoder.encode(customMessageBody, StandardCharsets.UTF_8);
            String encodedBcc = URLEncoder.encode(bccAggregatorString, StandardCharsets.UTF_8);

            String gmailUriTarget = String.format(
                    "https://mail.google.com/mail/?view=cm&fs=1&bcc=%s&su=%s&body=%s",
                    encodedBcc, encodedSubject, encodedBody
            );

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(gmailUriTarget));
                System.out.println(">> Mail Client Engine: External browser session launched successfully.");
            } else {
                throw new UnsupportedOperationException("Desktop browser control links are not supported on this host environment OS context runtime configuration.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("System Routing Failure", "Mailing Integration Dropped", "Unable to trigger external client: " + e.getMessage());
        }
    }
}