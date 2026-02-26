-- FUNCTION: public.tgf_sync_no_class()

-- DROP FUNCTION IF EXISTS public.tgf_sync_no_class();

CREATE OR REPLACE FUNCTION public.tgf_sync_no_class()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$
BEGIN
	NEW.is_labeled=NEW.is_no_class;
	RETURN NEW;
END;
$BODY$;

ALTER FUNCTION public.tgf_sync_no_class()
    OWNER TO postgres;
