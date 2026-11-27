CREATE TABLE productos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    categoria VARCHAR(100) NOT NULL,
    descripcion VARCHAR(1000),
    precio DECIMAL(12, 2) NOT NULL,
    imagen VARCHAR(2048),
    stock INT NOT NULL,

    CONSTRAINT pk_productos PRIMARY KEY (id),
    CONSTRAINT ck_productos_precio_positivo CHECK (precio > 0),
    CONSTRAINT ck_productos_stock_no_negativo CHECK (stock >= 0)
);
