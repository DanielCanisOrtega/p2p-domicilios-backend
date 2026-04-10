-- Agregar campos faltantes a la tabla DOMICILIARIO
ALTER TABLE public.domiciliario
ADD COLUMN IF NOT EXISTS user_id INTEGER,
ADD COLUMN IF NOT EXISTS vehiculo VARCHAR(50),
ADD COLUMN IF NOT EXISTS placa VARCHAR(20),
ADD COLUMN IF NOT EXISTS calificacion DECIMAL(3,2) DEFAULT 5.0;

-- Agregar FK a users
ALTER TABLE public.domiciliario
ADD CONSTRAINT fk_domiciliario_user
FOREIGN KEY (user_id) REFERENCES public.users(id)
ON DELETE CASCADE;

-- Agregar campos faltantes a USERS
ALTER TABLE public.users
ADD COLUMN IF NOT EXISTS numero_documento VARCHAR(20),
ADD COLUMN IF NOT EXISTS telefono VARCHAR(20);

-- Actualizar domiciliarios existentes (conectar con users donde role='DOMICILIARIO')
-- NOTA: Esto asume que tienes solo 1 usuario con role DOMICILIARIO con id=2
UPDATE public.domiciliario
SET user_id = 2
WHERE user_id IS NULL AND id_domiciliario = (
    SELECT MIN(id_domiciliario) FROM public.domiciliario WHERE user_id IS NULL
);

-- Agregar datos de prueba de vehículo
UPDATE public.domiciliario
SET
    vehiculo = 'Moto',
    placa = 'ABC-123',
    calificacion = 4.8
WHERE vehiculo IS NULL;

COMMENT ON COLUMN public.domiciliario.user_id IS 'FK al usuario que es domiciliario';
COMMENT ON COLUMN public.domiciliario.vehiculo IS 'Tipo de vehículo (Moto, Bicicleta, etc)';
COMMENT ON COLUMN public.domiciliario.placa IS 'Placa del vehículo';
COMMENT ON COLUMN public.domiciliario.calificacion IS 'Calificación promedio del domiciliario (0-5)';
