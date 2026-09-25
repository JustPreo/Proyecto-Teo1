CREATE OR ALTER TRIGGER tr_crear_subcategoria_default
ON categoria
AFTER INSERT
AS
BEGIN

    INSERT INTO subcategoria (
        id_categoria,
        nombre,
        descripcion,
        estado,
        es_default,
        creado_por
    )
    SELECT
        id_categoria,
        'General',
        'Subcategoria por defecto',
        1,
        1,
        creado_por
    FROM inserted
    --me salvo la vida un hindu

END;
