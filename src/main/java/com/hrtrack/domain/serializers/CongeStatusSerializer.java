package com.hrtrack.domain.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.hrtrack.domain.CongeStatus;

import java.io.IOException;

/**
 * Serializer personnalisé pour mapper les statuts de congé vers les valeurs attendues par le frontend.
 * ENATTENTE -> PENDING
 * ACCEPTER -> APPROVED
 * REFUSER -> REJECTED
 */
public class CongeStatusSerializer extends JsonSerializer<CongeStatus> {
    @Override
    public void serialize(CongeStatus value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        switch (value) {
            case ENATTENTE:
                gen.writeString("PENDING");
                break;
            case ACCEPTER:
                gen.writeString("APPROVED");
                break;
            case REFUSER:
                gen.writeString("REJECTED");
                break;
            default:
                gen.writeString(value.name());
        }
    }
}

