CREATE TABLE solicitudes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cliente_id BIGINT NOT NULL,
    mascota VARCHAR(100) NOT NULL,
    fecha_hora DATETIME(6) NOT NULL,
    motivo VARCHAR(500) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_creacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    fecha_actualizacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_solicitudes PRIMARY KEY (id),
    CONSTRAINT fk_solicitudes_cliente FOREIGN KEY (cliente_id)
        REFERENCES usuarios (id),
    CONSTRAINT ck_solicitudes_estado CHECK (
        estado IN ('PENDIENTE', 'CONFIRMADA', 'REPROGRAMADA', 'COMPLETADA', 'CANCELADA')
    )
);

CREATE INDEX ix_solicitudes_cliente_fecha
    ON solicitudes (cliente_id, fecha_hora);
