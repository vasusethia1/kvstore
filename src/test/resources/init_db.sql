--
-- PostgreSQL database dump
--

-- Dumped from database version 14.4
-- Dumped by pg_dump version 14.4

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: store; Type: TABLE; Schema: public; Owner: kojo
--

CREATE TABLE public.store (
    key character varying(255) NOT NULL,
    value text,
    ttl bigint
);


ALTER TABLE public.store OWNER TO kojo;

--
-- Data for Name: store; Type: TABLE DATA; Schema: public; Owner: kojo
--

COPY public.store (key, value, ttl) FROM stdin;
PeopleProfile:2	9176244239	1665918300
PeopleProfile:3	9176244238	1665939275
PeopleProfile:4	9176244238	1665940101
\.


--
-- Name: store store_pkey; Type: CONSTRAINT; Schema: public; Owner: kojo
--

ALTER TABLE ONLY public.store
    ADD CONSTRAINT store_pkey PRIMARY KEY (key);


--
-- Name: idx_store_ttl; Type: INDEX; Schema: public; Owner: kojo
--

CREATE INDEX idx_store_ttl ON public.store USING btree (ttl);


--
-- PostgreSQL database dump complete
--
