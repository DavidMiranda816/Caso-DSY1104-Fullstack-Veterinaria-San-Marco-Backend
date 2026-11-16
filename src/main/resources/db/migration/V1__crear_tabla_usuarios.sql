CREATE SEQUENCE seq_usuarios
    START WITH 1
    INCREMENT BY 1
    NOCYCLE;

CREATE TABLE usuarios (
    id              NUMBER(19)          NOT NULL,
    nombre          VARCHAR2(100 CHAR)  NOT NULL,
    correo          VARCHAR2(254 CHAR)  NOT NULL,
    password_hash   VARCHAR2(255 CHAR)  NOT NULL,
    rol             VARCHAR2(20 CHAR)   DEFAULT 'CLIENTE' NOT NULL,
    activo          NUMBER(1)           DEFAULT 1 NOT NULL,
    fecha_creacion  TIMESTAMP           DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT pk_usuarios PRIMARY KEY (id),
    CONSTRAINT uq_usuarios_correo UNIQUE (correo),
    CONSTRAINT ck_usuarios_rol CHECK (rol IN ('ADMIN', 'CLIENTE')),
    CONSTRAINT ck_usuarios_activo CHECK (activo IN (0, 1))
);