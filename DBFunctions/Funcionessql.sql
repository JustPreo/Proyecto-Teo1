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
END


SELECT dbo.fn_calcular_monto_ejecutado(2026, 8, 1)
       AS monto_ejecutado;