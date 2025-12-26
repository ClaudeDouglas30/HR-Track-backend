-- Migration pour créer la table des messages
-- Date: 2025-12-26

-- Création de la séquence pour les IDs
CREATE SEQUENCE IF NOT EXISTS messages_sequence START WITH 1 INCREMENT BY 100;

-- Création de la table messages
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT PRIMARY KEY DEFAULT nextval('messages_sequence'),
    message_uuid UUID NOT NULL UNIQUE,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    subject VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    date_envoi TIMESTAMP NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted_by_sender BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted_by_receiver BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_message_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_message_receiver FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Création des index pour optimiser les requêtes
CREATE INDEX IF NOT EXISTS idx_message_uuid ON messages(message_uuid);
CREATE INDEX IF NOT EXISTS idx_message_sender ON messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_message_receiver ON messages(receiver_id);
CREATE INDEX IF NOT EXISTS idx_message_date ON messages(date_envoi);
CREATE INDEX IF NOT EXISTS idx_message_read ON messages(is_read);
CREATE INDEX IF NOT EXISTS idx_message_receiver_not_deleted ON messages(receiver_id, is_deleted_by_receiver);

-- Commentaires sur la table et les colonnes
COMMENT ON TABLE messages IS 'Table pour stocker les messages entre utilisateurs';
COMMENT ON COLUMN messages.message_uuid IS 'Identifiant unique du message (UUID)';
COMMENT ON COLUMN messages.sender_id IS 'ID de l''utilisateur émetteur';
COMMENT ON COLUMN messages.receiver_id IS 'ID de l''utilisateur destinataire';
COMMENT ON COLUMN messages.subject IS 'Sujet du message';
COMMENT ON COLUMN messages.content IS 'Contenu du message';
COMMENT ON COLUMN messages.date_envoi IS 'Date et heure d''envoi du message';
COMMENT ON COLUMN messages.is_read IS 'Indicateur si le message a été lu par le destinataire';
COMMENT ON COLUMN messages.is_deleted_by_sender IS 'Indicateur si le message a été supprimé par l''émetteur';
COMMENT ON COLUMN messages.is_deleted_by_receiver IS 'Indicateur si le message a été supprimé par le destinataire';