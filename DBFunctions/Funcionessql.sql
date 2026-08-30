CREATE OR ALTER FUNCTION dbo.fn_obtener_categoria_por_subcategoria(@id_subcategoria INT)
RETURNS INT
AS
BEGIN
	DECLARE @id_cat INT
SELECT @id_cat = subc.id_categoria 
from subcategoria subc 
where @id_subcategoria = subc.id_subcategoria
return @id_cat
END;



CREATE OR ALTER FUNCTION dbo.fn_calcular_monto_ejecutado(@anio int ,@mes INT, @id_subcategoria INT)
RETURNS DECIMAL(12,2)--guardar 2 decimales y 10 numeros grandes osea max 9,999,999,999supongo
AS
BEGIN
	declare @monto decimal(12,2)
	select @monto = COALESCE(sum(t.monto),0) from transaccion t 
	where t.ano = @anio and t.mes = @mes and @id_subcategoria = t.id_subcategoria
	return @monto
END;



CREATE OR ALTER FUNCTION dbo.fn_validar_vigencia_presupuesto(@fecha DATE, @id_presupuesto INT)
RETURNS VARCHAR(1)
AS 
BEGIN
DECLARE @RESULTADO varchar(1) = 'N'
DECLARE @anio INT;
DECLARE @mes INT;

SET @anio = YEAR(@fecha);
SET @mes = MONTH(@fecha);

if exists (SELECT 1 from presupuesto p
where @id_presupuesto = p.id_presupuesto and 
		DATEFROMPARTS(@anio, @mes, 1)
          BETWEEN DATEFROMPARTS(p.ano_inicio, p.mes_inicio, 1)
          AND DATEFROMPARTS(p.ano_fin, p.mes_fin, 1))
          BEGIN
          SET @RESULTADO = 'S';
		  END;
	return @RESULTADO

	
END;


CREATE OR ALTER FUNCTION dbo.fn_obtener_total_ejecutado_categoria_mes(@id_categoria INT, @anio int, @mes INT)
RETURNS DECIMAL(12,2)
AS
BEGIN
	DECLARE @monto DECIMAL(12,2)
	SELECT @monto = coalesce(SUM(t.monto),0) from transaccion t 
	inner join subcategoria sub 
	on t.id_subcategoria = sub.id_subcategoria 
	and t.ano = @anio and t.mes = @mes
	and @id_categoria = sub.id_categoria
	
	return @monto
END



CREATE OR ALTER FUNCTION dbo.fn_obtener_total_categoria_mes(@id_categoria INT,
@id_presupuesto INT, @anio INT, @mes INT)
RETURNS DECIMAL(12,2)
AS 
BEGIN
	DECLARE @monto DECIMAL(12,2)
	SELECT @monto = COALESCE(SUM(pd.monto_mensual),0) FROM presupuesto p
	inner join presupuesto_detalle pd on p.id_presupuesto = pd.id_presupuesto
	inner join subcategoria sub on sub.id_subcategoria = pd.id_subcategoria
	where @id_presupuesto = pd.id_presupuesto and @id_categoria = sub.id_categoria 
	and DATEFROMPARTS(@anio, @mes, 1)
          BETWEEN DATEFROMPARTS(p.ano_inicio, p.mes_inicio, 1)
          AND DATEFROMPARTS(p.ano_fin, p.mes_fin, 1);
	
	return @monto
END


	
