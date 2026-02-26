-- FUNCTION: public.tgf_generate_track_id()

-- DROP FUNCTION IF EXISTS public.tgf_generate_track_id();

CREATE OR REPLACE FUNCTION public.tgf_generate_track_id()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$
BEGIN
    NEW.track_id = 'DatabricksADC-PJ'||NEW.project_id||'-SN'||NEW.snapshot_id||'-TR'||NEW.id;
    RETURN NEW;
END;
$BODY$;

ALTER FUNCTION public.tgf_generate_track_id()
    OWNER TO postgres;
