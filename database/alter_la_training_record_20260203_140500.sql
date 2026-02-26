ALTER TABLE IF EXISTS public.la_training_record
    ADD COLUMN snapshot_id bigint;
ALTER TABLE IF EXISTS public.la_training_record_aud
    ADD COLUMN snapshot_id bigint;
ALTER TABLE public.la_training_record
    ALTER COLUMN model_alias TYPE character varying(256) COLLATE pg_catalog."default";
ALTER TABLE public.la_training_record_aud
    ALTER COLUMN model_alias TYPE character varying(256) COLLATE pg_catalog."default";
ALTER TABLE public.la_training_record
    ALTER COLUMN track_id TYPE character varying(258) COLLATE pg_catalog."default";
ALTER TABLE public.la_training_record_aud
    ALTER COLUMN track_id TYPE character varying(258) COLLATE pg_catalog."default";
ALTER TABLE public.la_training_record
    ALTER COLUMN augmentation_param TYPE text COLLATE pg_catalog."default";
ALTER TABLE public.la_training_record_aud
    ALTER COLUMN augmentation_param TYPE text COLLATE pg_catalog."default";
ALTER TABLE IF EXISTS public.la_training_record
    ADD COLUMN model_track_key character varying(1024) COLLATE pg_catalog."default";
ALTER TABLE IF EXISTS public.la_training_record_aud
    ADD COLUMN model_track_key character varying(1024) COLLATE pg_catalog."default";