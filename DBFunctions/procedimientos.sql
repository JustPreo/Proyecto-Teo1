CREATE OR ALTER PROCEDURE dbo.sp_insertar_usuario
    @nombre VARCHAR(50),
    @apellido VARCHAR(50),
    @correo_electronico VARCHAR(100),
    @salario_base DECIMAL(12,2),
    @creado_por VARCHAR(100)
AS
BEGIN
	 INSERT INTO usuario
    (nombre,apellido,
     correo_electronico,salario_base,
     creado_por
    )
    VALUES
    (@nombre,@apellido,
     @correo_electronico,@salario_base,
     @creado_por);

END



CREATE OR ALTER PROCEDURE dbo.sp_actualizar_usuario
    @id_usuario INT,
    @nombre VARCHAR(50),
    @apellido VARCHAR(50),
    @correo_electronico VARCHAR(100),
    @salario_base DECIMAL(12,2),
    @modificado_por VARCHAR(100)
AS
BEGIN
	--revisar que correo no exista  y ver si existe usuario/activo
	--ahi poner algo como not exists y asi
	UPDATE usuario
    SET nombre = @nombre,
        apellido = @apellido,
        correo_electronico = @correo_electronico,
        salario_base = @salario_base,
        modificado_por = @modificado_por,
        modificado_en = SYSDATETIME()
    WHERE id_usuario = @id_usuario;

END



CREATE OR ALTER PROCEDURE dbo.sp_eliminar_usuario
    @id_usuario INT,@modificado_por VARCHAR(100)
AS
BEGIN
	--despues hacer verificacion si existe y si ya fue eliminado o no
	 UPDATE usuario
    SET estado = 0,modificado_por = @modificado_por,
    modificado_en = SYSDATETIME()
    WHERE id_usuario = @id_usuario;
END


CREATE OR ALTER PROCEDURE dbo.sp_consultar_usuario
    @id_usuario INT
AS
BEGIN
	SELECT * FROM usuario
	WHERE @id_usuario = id_usuario
END


CREATE OR ALTER PROCEDURE dbo.sp_listar_usuarios
AS
BEGIN
	SELECT * FROM usuario
    ORDER BY nombre, apellido;
END

-------------------------------------------------------------------------

CREATE OR ALTER PROCEDURE dbo.sp_insertar_categoria
    @nombre_categoria VARCHAR(50),@descripcion VARCHAR(255),
    @tipo_categoria SMALLINT,@order_presentacion SMALLINT,
    @creado_por VARCHAR(100)
AS
BEGIN

END;



CREATE OR ALTER PROCEDURE dbo.sp_actualizar_categoria
    @id_categoria INT,@nombre_categoria VARCHAR(50),
    @descripcion VARCHAR(255),@modificado_por VARCHAR(100)
AS
BEGIN

END;




CREATE OR ALTER PROCEDURE dbo.sp_eliminar_categoria
    @id_categoria INT,@modificado_por VARCHAR(100)
AS
BEGIN

END;



CREATE OR ALTER PROCEDURE dbo.sp_consultar_categoria
    @id_categoria INT
AS
BEGIN

END;


CREATE OR ALTER PROCEDURE dbo.sp_listar_categorias
    @tipo_categoria SMALLINT = NULL
    --tipo tendria que ser algo como if not null entonces esto y si es null entonces retornar todas
AS
BEGIN

END;










