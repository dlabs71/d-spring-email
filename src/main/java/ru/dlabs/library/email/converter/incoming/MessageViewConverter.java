package ru.dlabs.library.email.converter.incoming;

import static ru.dlabs.library.email.util.HttpUtils.CONTENT_TRANSFER_ENCODING_HDR;

import jakarta.mail.Address;
import jakarta.mail.Flags;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.angus.mail.imap.IMAPMessage;
import ru.dlabs.library.email.dto.message.common.EmailParticipant;
import ru.dlabs.library.email.dto.message.incoming.MessageView;
import ru.dlabs.library.email.exception.CheckEmailException;
import ru.dlabs.library.email.type.TransferEncoder;
import ru.dlabs.library.email.util.EmailMessageUtils;
import ru.dlabs.library.email.util.JavaCoreUtils;

/**
 * Utility class is for converting a {@link jakarta.mail.Message} to an instance of the {@link MessageView} class.
 *
 * <p>
 * <div><strong>Project name:</strong> d-email</div>
 * <div><strong>Creation date:</strong> 2023-09-01</div>
 * </p>
 *
 * @author Ivanov Danila
 * @since 1.0.0
 */
@Slf4j
@UtilityClass
public class MessageViewConverter {

    /**
     * It converts the message to an instance of the {@link MessageView} class.
     *
     * @param message the source message
     *
     * @return instance of the {@link MessageView} class
     */
    public MessageView convert(Message message) {
        if (message == null) {
            return null;
        }
        MessageView.MessageViewBuilder builder = MessageView.builder();
        builder.id(message.getMessageNumber());
        builder.recipients(MessagePartConverter.getRecipients(message));

        // Extraction of the subject
        try {
            if (message.getSubject() != null) {
                String decodedSubject = EmailMessageUtils.decodeData(message.getSubject());
                builder.subject(decodedSubject);
            }
        } catch (MessagingException e) {
            throw new CheckEmailException(
                "The attempt to get recipients of the message has failed: " + e.getMessage());
        }

        // Extraction of the sender address
        Address[] froms;
        try {
            froms = message.getFrom();
        } catch (MessagingException e) {
            throw new CheckEmailException(
                "The attempt to get senders of the message has failed: " + e.getMessage());
        }

        // Senders of email messages can be several. But we take only one — the first.
        if (froms != null && froms.length > 0) {
            InternetAddress internetAddress = (InternetAddress) froms[0];
            builder.sender(new EmailParticipant(internetAddress.getAddress(), internetAddress.getPersonal()));
        }

        // Set the read flag
        try {
            builder.seen(message.isSet(Flags.Flag.SEEN));
        } catch (MessagingException e) {
            log.warn(
                "It is impossible to determine whether a message has been flagged as seen. " + e.getMessage());
        }

        // Set a metadata of the message
        try {
            String transferEncoder = ((MimeMessage) message).getHeader(CONTENT_TRANSFER_ENCODING_HDR, null);
            builder.transferEncoder(TransferEncoder.forName(transferEncoder));

            if (message instanceof IMAPMessage) {
                builder.size(((IMAPMessage) message).getSizeLong());
            } else {
                builder.size((long) message.getSize());
            }

            if (message.getSentDate() != null) {
                builder.sentDate(JavaCoreUtils.convert(message.getSentDate()));
            }
            if (message.getReceivedDate() != null) {
                builder.receivedDate(JavaCoreUtils.convert(message.getReceivedDate()));
            }
        } catch (MessagingException e) {
            throw new CheckEmailException(
                "The attempt to get recipients of the message has failed: " + e.getMessage());
        }
        return builder.build();
    }
}
