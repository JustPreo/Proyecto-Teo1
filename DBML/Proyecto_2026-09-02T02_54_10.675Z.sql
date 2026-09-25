CREATE TABLE [usuario] (
	[id_usuario] INTEGER NOT NULL IDENTITY,
	[nombre] VARCHAR(50) NOT NULL,
	[apellido] VARCHAR(50) NOT NULL,
	[correo_electronico] VARCHAR(100) NOT NULL UNIQUE,
	[fecha_registro] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
	[salario_base] DECIMAL(12,2) NOT NULL CHECK(salario_base >= 0),
	[estado] BIT NOT NULL DEFAULT 1,
	[creado_por] VARCHAR(100) NOT NULL,
	[modificado_por] VARCHAR(100),
	[creado_en] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
	[modificado_en] DATETIME2,
	PRIMARY KEY([id_usuario])
);

CREATE TABLE [presupuesto] (
	[id_presupuesto] INTEGER NOT NULL IDENTITY,
	[id_usuario] INTEGER NOT NULL,
	[nombre_descriptivo] VARCHAR(50) NOT NULL,
	[ano_inicio] INTEGER NOT NULL,
	[mes_inicio] SMALLINT NOT NULL CHECK(mes_inicio BETWEEN 1 AND 12),
	[ano_fin] INTEGER NOT NULL,
	[mes_fin] SMALLINT NOT NULL CHECK(mes_fin BETWEEN 1 AND 12),
	[total_ingresos] DECIMAL(12,2) NOT NULL CHECK(total_ingresos >= 0),
	[total_gastos] DECIMAL(12,2) NOT NULL CHECK(total_gastos >= 0),
	[total_ahorro] DECIMAL(12,2) NOT NULL CHECK(total_ahorro >= 0),
	[fecha_hora_creacion] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
	[estado_presupuesto] SMALLINT NOT NULL DEFAULT 1 CHECK(estado_presupuesto IN (1,2,3)),
	[creado_por] VARCHAR(100) NOT NULL,
	[modificado_por] VARCHAR(100),
	[creado_en] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
	[modificado_en] DATETIME2,
	CHECK (ano_fin > ano_inicio OR (ano_fin = ano_inicio AND mes_fin >= mes_inicio)),
	PRIMARY KEY([id_presupuesto])
);

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description', @value=N'(1.activo/2.cerrado/3.borrador)',
    @level0type=N'SCHEMA',@level0name=N'dbo',
    @level1type=N'TABLE',@level1name=N'presupuesto',
    @level2type=N'COLUMN',@level2name=N'estado_presupuesto';

CREATE TABLE [categoria] (
	[id_categoria] INTEGER NOT NULL IDENTITY,
	[id_usuario] INTEGER NOT NULL,
	[nombre_categoria] VARCHAR(50) NOT NULL,
	[descripcion] VARCHAR(255),
	[tipo_categoria] SMALLINT NOT NULL CHECK(tipo_categoria IN (1,2,3)),
	[order_presentacion] SMALLINT NOT NULL CHECK(order_presentacion >= 0),
	[creado_por] VARCHAR(100) NOT NULL,
	[modificado_por] VARCHAR(100),
	[creado_en] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
	[modificado_en] DATETIME2,
	PRIMARY KEY([id_categoria])
);

CREATE UNIQUE INDEX [categoria_index_0]
ON [categoria] ([id_usuario], [nombre_categoria], [tipo_categoria]);

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description', @value=N'1.ingreso/2.gasto/3.ahorro',
    @level0type=N'SCHEMA',@level0name=N'dbo',
    @level1type=N'TABLE',@level1name=N'categoria',
    @level2type=N'COLUMN',@level2name=N'tipo_categoria';

CREATE TABLE [subcategoria] (
	[id_subcategoria] INTEGER NOT NULL IDENTITY,
	[id_categoria] INTEGER NOT NULL,
	[nombre] VARCHAR(50) NOT NULL,
	[descripcion] VARCHAR(255),
	[estado] BIT NOT NULL DEFAULT 1,
	[es_default] BIT NOT NULL DEFAULT 0,
	[creado_por] VARCHAR(100) NOT NULL,
	[modificado_por] VARCHAR(100),
	[creado_en] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
	[modificado_en] DATETIME2,
	PRIMARY KEY([id_subcategoria])
);

CREATE UNIQUE INDEX [subcategoria_index_0]
ON [subcategoria] ([id_categoria], [nombre]);

CREATE TABLE [presupuesto_detalle] (
	[id_detalle] INTEGER NOT NULL IDENTITY,
	[id_presupuesto] INTEGER NOT NULL,
	[id_subcategoria] INTEGER NOT NULL,
	[monto_mensual] DECIMAL(12,2) NOT NULL CHECK(monto_mensual >= 0),
	[justificacion_monto] VARCHAR(255),
	[creado_por] VARCHAR(100) NOT NULL,
	[modificado_por] VARCHAR(100),
	[creado_en] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
	[modificado_en] DATETIME2,
	PRIMARY KEY([id_detalle])
);

CREATE UNIQUE INDEX [presupuesto_detalle_index_0]
ON [presupuesto_detalle] ([id_presupuesto], [id_subcategoria]);

CREATE TABLE [obligacion_fija] (
	[id_obligacion] INTEGER NOT NULL IDENTITY,
	[id_usuario] INTEGER NOT NULL,
	[id_subcategoria] INTEGER NOT NULL,
	[nombre] VARCHAR(50) NOT NULL,
	[descripcion] VARCHAR(255),
	[monto_fijo_mensual] DECIMAL(12,2) NOT NULL CHECK(monto_fijo_mensual >= 0),
	[dia_vencimiento] SMALLINT NOT NULL CHECK(dia_vencimiento BETWEEN 1 AND 31),
	[vigente] BIT NOT NULL DEFAULT 1,
	[fecha_inicio] DATE NOT NULL,
	[fecha_fin] DATE,
	[creado_por] VARCHAR(100) NOT NULL,
	[modificado_por] VARCHAR(100),
	[creado_en] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
	[modificado_en] DATETIME2,
	CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio),
	PRIMARY KEY([id_obligacion])
);

CREATE TABLE [transaccion] (
	[id_transaccion] INTEGER NOT NULL IDENTITY,
	[id_usuario] INTEGER NOT NULL,
	[id_presupuesto] INTEGER NOT NULL,
	[id_subcategoria] INTEGER NOT NULL,
	[id_obligacion] INTEGER,
	[ano] INTEGER NOT NULL,
	[mes] SMALLINT NOT NULL CHECK(mes BETWEEN 1 AND 12),
	[tipo] SMALLINT NOT NULL CHECK(tipo IN (1,2,3)),
	[descripcion] VARCHAR(255) NOT NULL,
	[monto] DECIMAL(12,2) NOT NULL CHECK(monto > 0),
	[fecha] DATE NOT NULL,
	[metodo_pago] VARCHAR(50) NOT NULL CHECK(metodo_pago IN (     'efectivo',     'tarjeta_debito',     'tarjeta_credito',     'transferencia' )),
	[numero_factura] VARCHAR(50),
	[observaciones] VARCHAR(255),
	[fecha_registro] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
	[creado_por] VARCHAR(100) NOT NULL,
	[modificado_por] VARCHAR(100),
	[creado_en] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
	[modificado_en] DATETIME2,
	PRIMARY KEY([id_transaccion])
);


ALTER TABLE [presupuesto]
ADD FOREIGN KEY([id_usuario])
REFERENCES [usuario]([id_usuario])
ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE [categoria]
ADD FOREIGN KEY([id_usuario])
REFERENCES [usuario]([id_usuario])
ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE [subcategoria]
ADD FOREIGN KEY([id_categoria])
REFERENCES [categoria]([id_categoria])
ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE [presupuesto_detalle]
ADD FOREIGN KEY([id_subcategoria])
REFERENCES [subcategoria]([id_subcategoria])
ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE [presupuesto_detalle]
ADD FOREIGN KEY([id_presupuesto])
REFERENCES [presupuesto]([id_presupuesto])
ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE [obligacion_fija]
ADD FOREIGN KEY([id_usuario])
REFERENCES [usuario]([id_usuario])
ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE [obligacion_fija]
ADD FOREIGN KEY([id_subcategoria])
REFERENCES [subcategoria]([id_subcategoria])
ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE [transaccion]
ADD FOREIGN KEY([id_usuario])
REFERENCES [usuario]([id_usuario])
ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE [transaccion]
ADD FOREIGN KEY([id_presupuesto])
REFERENCES [presupuesto]([id_presupuesto])
ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE [transaccion]
ADD FOREIGN KEY([id_subcategoria])
REFERENCES [subcategoria]([id_subcategoria])
ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE [transaccion]
ADD FOREIGN KEY([id_obligacion])
REFERENCES [obligacion_fija]([id_obligacion])
ON UPDATE NO ACTION ON DELETE NO ACTION;
