package ru.dlabs.library.email.dto.message.outgoing;

import static ru.dlabs.library.email.util.HttpUtils.DEFAULT_ENCODING;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ru.dlabs.library.email.dto.message.common.ContentMessage;
import ru.dlabs.library.email.dto.message.common.EmailAttachment;
import ru.dlabs.library.email.dto.message.common.EmailParticipant;
import ru.dlabs.library.email.exception.TemplateCreationException;
import ru.dlabs.library.email.util.TemplateUtils;

/**
 * Class describe a templated outgoing email message.
 * If you want to send an HTML message, you can use this class to do it. Library use velocity template
 *
 * @author Ivanov Danila
 * @version 1.0
 * @see <a href="https://velocity.apache.org/engine/1.7/user-guide.html#what-is-velocity">Apache Velocity Project</a>
 */
@Getter
@ToString
public class TemplatedOutgoingMessage extends DefaultOutgoingMessage {

    /**
     * Path to velocity template
     */
    private final String pathToTemplate;

    /**
     * It's values for aliases in the velocity template
     */
    private final Map<String, Object> params;

    private final OutgoingContentType contentType;

    public TemplatedOutgoingMessage(
        String subject,
        String pathToTemplate,
        Map<String, Object> params,
        Set<EmailParticipant> recipientEmail
    ) throws TemplateCreationException {
        this(subject, pathToTemplate, params, null, OutgoingContentType.HTML, recipientEmail, null);
    }

    public TemplatedOutgoingMessage(
        String subject,
        String pathToTemplate,
        Map<String, Object> params,
        Set<EmailParticipant> recipientEmail,
        List<EmailAttachment> attachments
    ) throws TemplateCreationException {
        this(subject, pathToTemplate, params, null, OutgoingContentType.HTML, recipientEmail, attachments);
    }

    public TemplatedOutgoingMessage(
        String subject,
        String pathToTemplate,
        Map<String, Object> params,
        String encoding,
        OutgoingContentType contentType,
        Set<EmailParticipant> recipientEmail
    ) throws TemplateCreationException {
        this(subject, pathToTemplate, params, encoding, contentType, recipientEmail, null);
    }

    public TemplatedOutgoingMessage(
        String subject,
        String pathToTemplate,
        Map<String, Object> params,
        String encoding,
        OutgoingContentType contentType,
        Set<EmailParticipant> recipientEmail,
        List<EmailAttachment> attachments
    ) throws TemplateCreationException {
        super(subject, encoding, null, contentType, recipientEmail, attachments);
        this.pathToTemplate = pathToTemplate;
        this.params = params;
        this.contentType = contentType;
        String content = this.constructContent();
        this.addContent(new ContentMessage(content, contentType.getContentType(), encoding));
    }

    public static TemplatedMessageBuilder builder() { return new TemplatedMessageBuilder(); }

    public String constructContent() throws TemplateCreationException {
        if (pathToTemplate == null || params == null) {
            return null;
        }
        return TemplateUtils.construct(pathToTemplate, params);
    }

    @ToString
    @NoArgsConstructor
    public static class TemplatedMessageBuilder {

        private String pathToTemplate;
        private Map<String, Object> params = new HashMap<>();
        private String subject;
        private OutgoingContentType contentType = OutgoingContentType.HTML;
        private Set<EmailParticipant> recipientEmail = new HashSet<>();
        private List<EmailAttachment> attachments = new ArrayList<>();
        private String encoding = DEFAULT_ENCODING;


        public TemplatedOutgoingMessage build() throws TemplateCreationException {
            return new TemplatedOutgoingMessage(
                subject,
                pathToTemplate,
                params,
                encoding,
                contentType,
                recipientEmail,
                attachments
            );
        }

        public TemplatedMessageBuilder template(String pathToTemplate, Map<String, Object> params) {
            this.pathToTemplate = pathToTemplate;
            this.params = params;
            return this;
        }

        public TemplatedMessageBuilder template(String pathToTemplate) {
            this.pathToTemplate = pathToTemplate;
            return this;
        }

        public TemplatedMessageBuilder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public TemplatedMessageBuilder recipientEmail(Set<EmailParticipant> recipientEmail) {
            this.recipientEmail = recipientEmail;
            return this;
        }

        public TemplatedMessageBuilder attachments(List<EmailAttachment> attachments) {
            this.attachments = attachments;
            return this;
        }

        public TemplatedMessageBuilder contentType(OutgoingContentType contentType) {
            this.contentType = contentType;
            return this;
        }

        public TemplatedMessageBuilder encoding(String encoding) {
            this.encoding = encoding;
            return this;
        }

        public TemplatedMessageBuilder addAttachment(EmailAttachment attachment) {
            if (this.attachments == null) {
                this.attachments = new ArrayList<>();
            }
            this.attachments.add(attachment);
            return this;
        }
    }
}