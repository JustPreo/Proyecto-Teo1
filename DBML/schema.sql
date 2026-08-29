CREATE TABLE [cliente] (
	[cliente_id] INT NOT NULL IDENTITY,
	[nombre] VARCHAR(50),
	PRIMARY KEY([cliente_id])
);
GO

CREATE TABLE [direccion] (
	[cliente_id] INT NOT NULL IDENTITY,
	[numero_direccion] INT,
	[ciudad] VARCHAR(255),
	[calle] VARCHAR(255),
	[pais] VARCHAR(255),
	PRIMARY KEY([cliente_id])
);
GO

CREATE TABLE [documento] (
	[numero_documento] INT NOT NULL IDENTITY,
	[cliente_id] INT,
	[fecha] DATETIME2,
	[monto] DECIMAL(18,2),
	PRIMARY KEY([numero_documento])
);
GO

CREATE TABLE [factura] (
	[numero_documento] INT NOT NULL IDENTITY,
	[fecha_pago] DATETIME2,
	[tipo_factura] VARCHAR(50),
	[impuesto] DECIMAL(18,2),
	PRIMARY KEY([numero_documento])
);
GO

CREATE TABLE [usuario] (
	[id_usuario] INT NOT NULL,
	[nombre] VARCHAR(50) NOT NULL,
	[apellido] VARCHAR(50) NOT NULL,
	[correo_electronico] VARCHAR(100) NOT NULL UNIQUE,
	[fecha_registro] DATETIME2 NOT NULL,
	[salario_base] DECIMAL(18,2) NOT NULL,
	[estado] BIT NOT NULL,
	PRIMARY KEY([id_usuario])
);
GO

CREATE TABLE [presupuesto] (
	[id_presupuesto] INT NOT NULL,
	[id_usuario] INT NOT NULL,
	[nombre_descriptivo] VARCHAR(50) NOT NULL,
	[ano_inicio] INT NOT NULL,
	[mes_inicio] SMALLINT NOT NULL,
	[ano_fin] INT NOT NULL,
	[mes_fin] SMALLINT NOT NULL,
	[total_ingresos] DECIMAL(18,2) NOT NULL,
	[total_gastos] DECIMAL(18,2) NOT NULL,
	[total_ahorro] DECIMAL(18,2) NOT NULL,
	[fecha_hora_creacion] DATETIME2 NOT NULL,
	[estado_presupuesto] SMALLINT NOT NULL,
	PRIMARY KEY([id_presupuesto])
);
GO

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description', @value=N'(1.activo/2.cerrado/3.borrador)',
    @level0type=N'SCHEMA',@level0name=N'dbo',
    @level1type=N'TABLE',@level1name=N'presupuesto',
    @level2type=N'COLUMN',@level2name=N'estado_presupuesto';
GO

CREATE TABLE [categoria] (
	[id_categoria] INT NOT NULL,
	[nombre_categoria] VARCHAR(50) NOT NULL,
	[descripcion] VARCHAR(255),
	[tipo_categoria] SMALLINT NOT NULL,
	[order_presentacion] SMALLINT NOT NULL,
	PRIMARY KEY([id_categoria])
);
GO

EXEC sys.sp_addextendedproperty
    @name=N'MS_Description', @value=N'1.ingreso/2.gasto/3.ahorro',
    @level0type=N'SCHEMA',@level0name=N'dbo',
    @level1type=N'TABLE',@level1name=N'categoria',
    @level2type=N'COLUMN',@level2name=N'tipo_categoria';
GO

CREATE TABLE [subcategoria] (
	[id_subcategoria] INT NOT NULL,
	[id_categoria] INT NOT NULL,
	[nombre] VARCHAR(50) NOT NULL,
	[descripcion] VARCHAR(255),
	[estado] BIT NOT NULL,
	[default] BIT NOT NULL,
	PRIMARY KEY([id_subcategoria])
);
GO

CREATE TABLE [presupuesto_detalle] (
	[id_detalle] INT NOT NULL,
	[id_presupuesto] INT NOT NULL,
	[id_subcategoria] INT NOT NULL,
	[monto_mensual] DECIMAL(18,2) NOT NULL,
	[justificacion_monto] VARCHAR(255),
	PRIMARY KEY([id_detalle])
);
GO

CREATE TABLE [obligacion_fija] (
	[id_obligacion] INT NOT NULL,
	[id_usuario] INT NOT NULL,
	[id_subcategoria] INT NOT NULL,
	[nombre] VARCHAR(50) NOT NULL,
	[descripcion] VARCHAR(255),
	[monto_fijo_mensual] DECIMAL(18,2) NOT NULL,
	[dia_vencimiento] SMALLINT NOT NULL,
	[vigente] BIT NOT NULL,
	[fecha_inicio] DATE NOT NULL,
	[fecha_fin] DATE,
	PRIMARY KEY([id_obligacion])
);
GO

CREATE TABLE [transaccion] (
	[id_transaccion] INT NOT NULL,
	[id_usuario] INT NOT NULL,
	[id_presupuesto] INT NOT NULL,
	[id_subcategoria] INT NOT NULL,
	[id_obligacion] INT,
	[ano] INT NOT NULL,
	[mes] SMALLINT NOT NULL,
	[tipo] SMALLINT NOT NULL,
	[descripcion] VARCHAR(255) NOT NULL,
	[monto] DECIMAL(18,2) NOT NULL,
	[fecha] DATE NOT NULL,
	[metodo_pago] VARCHAR(50) NOT NULL,
	[numero_factura] VARCHAR(50),
	[observaciones] VARCHAR(255),
	[fecha_registro] DATETIME2 NOT NULL,
	PRIMARY KEY([id_transaccion])
);
GO

ALTER TABLE [direccion]
ADD FOREIGN KEY([cliente_id])
REFERENCES [cliente]([cliente_id])
ON UPDATE NO ACTION ON DELETE NO ACTION;
GO
ALTER TABLE [documento]
ADD FOREIGN KEY([cliente_id])
REFERENCES [cliente]([cliente_id])
ON UPDATE NO ACTION ON DELETE NO ACTION;
GO
ALTER TABLE [factura]
ADD FOREIGN KEY([numero_documento])
REFERENCES [documento]([numero_documento])
ON UPDATE NO ACTION ON DELETE NO ACTION;
GO
ALTER TABLE [presupuesto]
ADD FOREIGN KEY([id_usuario])
REFERENCES [usuario]([id_usuario])
ON UPDATE NO ACTION ON DELETE NO ACTION;
GO
ALTER TABLE [subcategoria]
ADD FOREIGN KEY([id_categoria])
REFERENCES [categoria]([id_categoria])
ON UPDATE NO ACTION ON DELETE NO ACTION;
GO
ALTER TABLE [presupuesto_detalle]
ADD FOREIGN KEY([id_subcategoria])
REFERENCES [subcategoria]([id_subcategoria])
ON UPDATE NO ACTION ON DELETE NO ACTION;
GO
ALTER TABLE [presupuesto_detalle]
ADD FOREIGN KEY([id_presupuesto])
REFERENCES [presupuesto]([id_presupuesto])
ON UPDATE NO ACTION ON DELETE NO ACTION;
GO
ALTER TABLE [obligacion_fija]
ADD FOREIGN KEY([id_usuario])
REFERENCES [usuario]([id_usuario])
ON UPDATE NO ACTION ON DELETE NO ACTION;
GO
ALTER TABLE [obligacion_fija]
ADD FOREIGN KEY([id_subcategoria])
REFERENCES [subcategoria]([id_subcategoria])
ON UPDATE NO ACTION ON DELETE NO ACTION;
GO
ALTER TABLE [transaccion]
ADD FOREIGN KEY([id_usuario])
REFERENCES [usuario]([id_usuario])
ON UPDATE NO ACTION ON DELETE NO ACTION;
GO
ALTER TABLE [transaccion]
ADD FOREIGN KEY([id_presupuesto])
REFERENCES [presupuesto]([id_presupuesto])
ON UPDATE NO ACTION ON DELETE NO ACTION;
GO
ALTER TABLE [transaccion]
ADD FOREIGN KEY([id_subcategoria])
REFERENCES [subcategoria]([id_subcategoria])
ON UPDATE NO ACTION ON DELETE NO ACTION;
GO
ALTER TABLE [transaccion]
ADD FOREIGN KEY([id_obligacion])
REFERENCES [obligacion_fija]([id_obligacion])
ON UPDATE NO ACTION ON DELETE NO ACTION;
GO
