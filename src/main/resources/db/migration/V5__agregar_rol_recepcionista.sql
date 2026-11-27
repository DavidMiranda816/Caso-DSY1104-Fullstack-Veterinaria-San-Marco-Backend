ALTER TABLE usuarios
    DROP CHECK ck_usuarios_rol_validos,
    ADD CONSTRAINT ck_usuarios_rol_validos
        CHECK (rol IN (
            'ADMIN',
            'CLIENTE',
            'RECEPCIONISTA',
            'VENDEDOR',
            'ENCARGADO'
        ));
