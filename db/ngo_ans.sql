--
-- PostgreSQL database dump
--

\restrict vfjIIb93aycyW4mWVDfXuz8IujEJqouMsyJcTXqIypOLmnH1BS4MSe9XbnScsYW

-- Dumped from database version 18.6
-- Dumped by pg_dump version 18.6

-- Started on 2026-09-19 00:22:18

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
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
-- TOC entry 234 (class 1259 OID 18127)
-- Name: asignacion; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.asignacion (
    id_asignacion bigint NOT NULL,
    id_solicitud bigint NOT NULL,
    id_usuario bigint NOT NULL,
    fecha_asignacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.asignacion OWNER TO postgres;

--
-- TOC entry 233 (class 1259 OID 18126)
-- Name: asignacion_id_asignacion_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.asignacion_id_asignacion_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.asignacion_id_asignacion_seq OWNER TO postgres;

--
-- TOC entry 5116 (class 0 OID 0)
-- Dependencies: 233
-- Name: asignacion_id_asignacion_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.asignacion_id_asignacion_seq OWNED BY public.asignacion.id_asignacion;


--
-- TOC entry 220 (class 1259 OID 17946)
-- Name: cliente; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.cliente (
    id_cliente bigint NOT NULL,
    nombre character varying(100) NOT NULL,
    documento character varying(20) NOT NULL,
    telefono character varying(20) NOT NULL,
    correo character varying(100),
    CONSTRAINT cliente_documento_check CHECK (((documento)::text ~ '^[0-9]+$'::text)),
    CONSTRAINT cliente_telefono_check CHECK (((telefono)::text ~ '^\+?[0-9]+$'::text))
);


ALTER TABLE public.cliente OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 17945)
-- Name: cliente_id_cliente_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.cliente_id_cliente_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.cliente_id_cliente_seq OWNER TO postgres;

--
-- TOC entry 5117 (class 0 OID 0)
-- Dependencies: 219
-- Name: cliente_id_cliente_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.cliente_id_cliente_seq OWNED BY public.cliente.id_cliente;


--
-- TOC entry 232 (class 1259 OID 18104)
-- Name: garantia; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.garantia (
    id_garantia bigint NOT NULL,
    id_cliente bigint NOT NULL,
    id_producto bigint NOT NULL,
    fecha_inicio date NOT NULL,
    fecha_fin date NOT NULL,
    estado character varying(30) NOT NULL
);


ALTER TABLE public.garantia OWNER TO postgres;

--
-- TOC entry 231 (class 1259 OID 18103)
-- Name: garantia_id_garantia_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.garantia_id_garantia_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.garantia_id_garantia_seq OWNER TO postgres;

--
-- TOC entry 5118 (class 0 OID 0)
-- Dependencies: 231
-- Name: garantia_id_garantia_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.garantia_id_garantia_seq OWNED BY public.garantia.id_garantia;


--
-- TOC entry 222 (class 1259 OID 17959)
-- Name: producto; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.producto (
    id_producto bigint NOT NULL,
    marca character varying(50) NOT NULL,
    modelo character varying(50) NOT NULL,
    nro_serie character varying(50) NOT NULL,
    tipo_producto character varying(50) NOT NULL
);


ALTER TABLE public.producto OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 17958)
-- Name: producto_id_producto_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.producto_id_producto_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.producto_id_producto_seq OWNER TO postgres;

--
-- TOC entry 5119 (class 0 OID 0)
-- Dependencies: 221
-- Name: producto_id_producto_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.producto_id_producto_seq OWNED BY public.producto.id_producto;


--
-- TOC entry 226 (class 1259 OID 18060)
-- Name: rol; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.rol (
    id_rol bigint NOT NULL,
    nombre character varying(50) NOT NULL,
    descripcion character varying(255)
);


ALTER TABLE public.rol OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 18059)
-- Name: rol_id_rol_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.rol_id_rol_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.rol_id_rol_seq OWNER TO postgres;

--
-- TOC entry 5120 (class 0 OID 0)
-- Dependencies: 225
-- Name: rol_id_rol_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.rol_id_rol_seq OWNED BY public.rol.id_rol;


--
-- TOC entry 236 (class 1259 OID 18148)
-- Name: seguimiento; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.seguimiento (
    id_seguimiento bigint NOT NULL,
    id_solicitud bigint NOT NULL,
    id_usuario bigint NOT NULL,
    fecha timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    estado character varying(30) NOT NULL,
    diagnostico text,
    observacion text
);


ALTER TABLE public.seguimiento OWNER TO postgres;

--
-- TOC entry 235 (class 1259 OID 18147)
-- Name: seguimiento_id_seguimiento_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.seguimiento_id_seguimiento_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.seguimiento_id_seguimiento_seq OWNER TO postgres;

--
-- TOC entry 5121 (class 0 OID 0)
-- Dependencies: 235
-- Name: seguimiento_id_seguimiento_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.seguimiento_id_seguimiento_seq OWNED BY public.seguimiento.id_seguimiento;


--
-- TOC entry 228 (class 1259 OID 18069)
-- Name: servicio_autorizado; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.servicio_autorizado (
    id_servicio bigint NOT NULL,
    nombre character varying(100) NOT NULL,
    ciudad character varying(100),
    estado character varying(30) DEFAULT 'ACTIVO'::character varying
);


ALTER TABLE public.servicio_autorizado OWNER TO postgres;

--
-- TOC entry 227 (class 1259 OID 18068)
-- Name: servicio_autorizado_id_servicio_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.servicio_autorizado_id_servicio_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.servicio_autorizado_id_servicio_seq OWNER TO postgres;

--
-- TOC entry 5122 (class 0 OID 0)
-- Dependencies: 227
-- Name: servicio_autorizado_id_servicio_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.servicio_autorizado_id_servicio_seq OWNED BY public.servicio_autorizado.id_servicio;


--
-- TOC entry 224 (class 1259 OID 17973)
-- Name: solicitud; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.solicitud (
    id_solicitud bigint NOT NULL,
    id_cliente bigint NOT NULL,
    id_producto bigint NOT NULL,
    fecha timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    descripcion text NOT NULL,
    estado_actual character varying(30) DEFAULT 'RECIBIDA'::character varying NOT NULL,
    id_garantia bigint
);


ALTER TABLE public.solicitud OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 17972)
-- Name: solicitud_id_solicitud_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.solicitud_id_solicitud_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.solicitud_id_solicitud_seq OWNER TO postgres;

--
-- TOC entry 5123 (class 0 OID 0)
-- Dependencies: 223
-- Name: solicitud_id_solicitud_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.solicitud_id_solicitud_seq OWNED BY public.solicitud.id_solicitud;


--
-- TOC entry 230 (class 1259 OID 18079)
-- Name: usuario; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.usuario (
    id_usuario bigint NOT NULL,
    id_rol bigint NOT NULL,
    id_servicio bigint,
    nombre character varying(100) NOT NULL,
    correo character varying(100) NOT NULL,
    clave character varying(255) NOT NULL,
    estado character varying(30) DEFAULT 'ACTIVO'::character varying
);


ALTER TABLE public.usuario OWNER TO postgres;

--
-- TOC entry 229 (class 1259 OID 18078)
-- Name: usuario_id_usuario_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.usuario_id_usuario_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.usuario_id_usuario_seq OWNER TO postgres;

--
-- TOC entry 5124 (class 0 OID 0)
-- Dependencies: 229
-- Name: usuario_id_usuario_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.usuario_id_usuario_seq OWNED BY public.usuario.id_usuario;


--
-- TOC entry 4907 (class 2604 OID 18177)
-- Name: asignacion id_asignacion; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asignacion ALTER COLUMN id_asignacion SET DEFAULT nextval('public.asignacion_id_asignacion_seq'::regclass);


--
-- TOC entry 4896 (class 2604 OID 17998)
-- Name: cliente id_cliente; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cliente ALTER COLUMN id_cliente SET DEFAULT nextval('public.cliente_id_cliente_seq'::regclass);


--
-- TOC entry 4906 (class 2604 OID 18205)
-- Name: garantia id_garantia; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.garantia ALTER COLUMN id_garantia SET DEFAULT nextval('public.garantia_id_garantia_seq'::regclass);


--
-- TOC entry 4897 (class 2604 OID 18011)
-- Name: producto id_producto; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.producto ALTER COLUMN id_producto SET DEFAULT nextval('public.producto_id_producto_seq'::regclass);


--
-- TOC entry 4901 (class 2604 OID 18238)
-- Name: rol id_rol; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rol ALTER COLUMN id_rol SET DEFAULT nextval('public.rol_id_rol_seq'::regclass);


--
-- TOC entry 4909 (class 2604 OID 18251)
-- Name: seguimiento id_seguimiento; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.seguimiento ALTER COLUMN id_seguimiento SET DEFAULT nextval('public.seguimiento_id_seguimiento_seq'::regclass);


--
-- TOC entry 4902 (class 2604 OID 18285)
-- Name: servicio_autorizado id_servicio; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.servicio_autorizado ALTER COLUMN id_servicio SET DEFAULT nextval('public.servicio_autorizado_id_servicio_seq'::regclass);


--
-- TOC entry 4898 (class 2604 OID 18025)
-- Name: solicitud id_solicitud; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitud ALTER COLUMN id_solicitud SET DEFAULT nextval('public.solicitud_id_solicitud_seq'::regclass);


--
-- TOC entry 4904 (class 2604 OID 18309)
-- Name: usuario id_usuario; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario ALTER COLUMN id_usuario SET DEFAULT nextval('public.usuario_id_usuario_seq'::regclass);


--
-- TOC entry 5108 (class 0 OID 18127)
-- Dependencies: 234
-- Data for Name: asignacion; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.asignacion (id_asignacion, id_solicitud, id_usuario, fecha_asignacion) FROM stdin;
\.


--
-- TOC entry 5094 (class 0 OID 17946)
-- Dependencies: 220
-- Data for Name: cliente; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.cliente (id_cliente, nombre, documento, telefono, correo) FROM stdin;
1	Cliente de Prueba	1234567	0981222333	prueba@test.com
2	Claudio Jara	5099215	0991979584	claudiojara@gmail.com
3	Prueba Prueba	1234567	0991789123	asdfg@gmail.com
4	Soto	5888111	0982400900	soto@gmail.com
5	María Gómez	4123456	+595981123456	maria.gomez@example.com
6	Sofía Ramírez	6234567	+595985567890	sofia.ramirez@example.com
7	Luis Villalba	2789012	+595984456789	\N
8	Carlos Benítez	3567890	+595982234567	carlos.benitez@example.com
9	Ana Duarte	5012345	0983345678	ana.duarte@example.com
10	Luis Soto	4111222	0994856271	soto@gmail.com
\.


--
-- TOC entry 5106 (class 0 OID 18104)
-- Dependencies: 232
-- Data for Name: garantia; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.garantia (id_garantia, id_cliente, id_producto, fecha_inicio, fecha_fin, estado) FROM stdin;
1	8	11	2026-05-30	2027-05-30	VIGENTE
2	9	12	2025-12-05	2026-06-05	VENCIDA
3	5	10	2025-06-15	2026-06-15	VENCIDA
4	6	13	2026-08-01	2027-08-01	VIGENTE
5	5	5	2026-03-10	2027-03-10	VIGENTE
6	7	8	2025-10-01	2026-10-01	VIGENTE
7	8	6	2024-05-02	2025-05-02	VENCIDA
8	1	1	2026-01-20	2027-01-20	VIGENTE
9	7	14	2026-09-10	2027-09-10	VIGENTE
10	6	9	2026-01-15	2028-01-15	VIGENTE
11	9	7	2025-11-20	2027-11-20	VIGENTE
\.


--
-- TOC entry 5096 (class 0 OID 17959)
-- Dependencies: 222
-- Data for Name: producto; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.producto (id_producto, marca, modelo, nro_serie, tipo_producto) FROM stdin;
1	NGO	Climatizador A1	SN-00012345	Equipo de climatizacion
2	Tokyo	qwes	2	Televisor
3	Tokyo	789	7894515	Televisor
4	Samsung	7894516	45689186	Vibrador
5	Samsung	Crystal UHD 55	SN-TV-0001	Televisor
6	LG	UR7800 43	SN-TV-0002	Televisor
7	Whirlpool	Frost Free 375L	SN-HE-0001	Heladera
8	Samsung	Twin Cooling 380L	SN-HE-0002	Heladera
9	Midea	Inverter 12000 BTU	SN-AA-0001	Aire acondicionado
10	LG	Dual Inverter 18000 BTU	SN-AA-0002	Aire acondicionado
11	Electrolux	EcoWash 10kg	SN-LA-0001	Lavarropas
12	Samsung	Galaxy A54	SN-CE-0001	Celular
13	Lenovo	IdeaPad 3 15	SN-NB-0001	Notebook
14	Philips	Airfryer XL	SN-FR-0001	Freidora de aire
\.


--
-- TOC entry 5100 (class 0 OID 18060)
-- Dependencies: 226
-- Data for Name: rol; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.rol (id_rol, nombre, descripcion) FROM stdin;
\.


--
-- TOC entry 5110 (class 0 OID 18148)
-- Dependencies: 236
-- Data for Name: seguimiento; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.seguimiento (id_seguimiento, id_solicitud, id_usuario, fecha, estado, diagnostico, observacion) FROM stdin;
\.


--
-- TOC entry 5102 (class 0 OID 18069)
-- Dependencies: 228
-- Data for Name: servicio_autorizado; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.servicio_autorizado (id_servicio, nombre, ciudad, estado) FROM stdin;
\.


--
-- TOC entry 5098 (class 0 OID 17973)
-- Dependencies: 224
-- Data for Name: solicitud; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.solicitud (id_solicitud, id_cliente, id_producto, fecha, descripcion, estado_actual, id_garantia) FROM stdin;
5	10	13	2026-09-19 00:16:57.840646	No carga	RECIBIDA	4
\.


--
-- TOC entry 5104 (class 0 OID 18079)
-- Dependencies: 230
-- Data for Name: usuario; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.usuario (id_usuario, id_rol, id_servicio, nombre, correo, clave, estado) FROM stdin;
\.


--
-- TOC entry 5125 (class 0 OID 0)
-- Dependencies: 233
-- Name: asignacion_id_asignacion_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.asignacion_id_asignacion_seq', 1, false);


--
-- TOC entry 5126 (class 0 OID 0)
-- Dependencies: 219
-- Name: cliente_id_cliente_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.cliente_id_cliente_seq', 10, true);


--
-- TOC entry 5127 (class 0 OID 0)
-- Dependencies: 231
-- Name: garantia_id_garantia_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.garantia_id_garantia_seq', 11, true);


--
-- TOC entry 5128 (class 0 OID 0)
-- Dependencies: 221
-- Name: producto_id_producto_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.producto_id_producto_seq', 14, true);


--
-- TOC entry 5129 (class 0 OID 0)
-- Dependencies: 225
-- Name: rol_id_rol_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.rol_id_rol_seq', 1, false);


--
-- TOC entry 5130 (class 0 OID 0)
-- Dependencies: 235
-- Name: seguimiento_id_seguimiento_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.seguimiento_id_seguimiento_seq', 1, false);


--
-- TOC entry 5131 (class 0 OID 0)
-- Dependencies: 227
-- Name: servicio_autorizado_id_servicio_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.servicio_autorizado_id_servicio_seq', 1, false);


--
-- TOC entry 5132 (class 0 OID 0)
-- Dependencies: 223
-- Name: solicitud_id_solicitud_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.solicitud_id_solicitud_seq', 5, true);


--
-- TOC entry 5133 (class 0 OID 0)
-- Dependencies: 229
-- Name: usuario_id_usuario_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.usuario_id_usuario_seq', 1, false);


--
-- TOC entry 4932 (class 2606 OID 18179)
-- Name: asignacion asignacion_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asignacion
    ADD CONSTRAINT asignacion_pkey PRIMARY KEY (id_asignacion);


--
-- TOC entry 4914 (class 2606 OID 18000)
-- Name: cliente cliente_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cliente
    ADD CONSTRAINT cliente_pkey PRIMARY KEY (id_cliente);


--
-- TOC entry 4930 (class 2606 OID 18207)
-- Name: garantia garantia_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.garantia
    ADD CONSTRAINT garantia_pkey PRIMARY KEY (id_garantia);


--
-- TOC entry 4916 (class 2606 OID 17971)
-- Name: producto producto_nro_serie_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.producto
    ADD CONSTRAINT producto_nro_serie_key UNIQUE (nro_serie);


--
-- TOC entry 4918 (class 2606 OID 18013)
-- Name: producto producto_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.producto
    ADD CONSTRAINT producto_pkey PRIMARY KEY (id_producto);


--
-- TOC entry 4922 (class 2606 OID 18240)
-- Name: rol rol_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rol
    ADD CONSTRAINT rol_pkey PRIMARY KEY (id_rol);


--
-- TOC entry 4934 (class 2606 OID 18253)
-- Name: seguimiento seguimiento_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.seguimiento
    ADD CONSTRAINT seguimiento_pkey PRIMARY KEY (id_seguimiento);


--
-- TOC entry 4924 (class 2606 OID 18287)
-- Name: servicio_autorizado servicio_autorizado_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.servicio_autorizado
    ADD CONSTRAINT servicio_autorizado_pkey PRIMARY KEY (id_servicio);


--
-- TOC entry 4920 (class 2606 OID 18027)
-- Name: solicitud solicitud_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitud
    ADD CONSTRAINT solicitud_pkey PRIMARY KEY (id_solicitud);


--
-- TOC entry 4926 (class 2606 OID 18092)
-- Name: usuario usuario_correo_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_correo_key UNIQUE (correo);


--
-- TOC entry 4928 (class 2606 OID 18311)
-- Name: usuario usuario_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_pkey PRIMARY KEY (id_usuario);


--
-- TOC entry 4942 (class 2606 OID 18186)
-- Name: asignacion asignacion_id_solicitud_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asignacion
    ADD CONSTRAINT asignacion_id_solicitud_fkey FOREIGN KEY (id_solicitud) REFERENCES public.solicitud(id_solicitud);


--
-- TOC entry 4943 (class 2606 OID 18313)
-- Name: asignacion asignacion_id_usuario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asignacion
    ADD CONSTRAINT asignacion_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario);


--
-- TOC entry 4940 (class 2606 OID 18219)
-- Name: garantia garantia_id_cliente_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.garantia
    ADD CONSTRAINT garantia_id_cliente_fkey FOREIGN KEY (id_cliente) REFERENCES public.cliente(id_cliente);


--
-- TOC entry 4941 (class 2606 OID 18229)
-- Name: garantia garantia_id_producto_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.garantia
    ADD CONSTRAINT garantia_id_producto_fkey FOREIGN KEY (id_producto) REFERENCES public.producto(id_producto);


--
-- TOC entry 4944 (class 2606 OID 18262)
-- Name: seguimiento seguimiento_id_solicitud_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.seguimiento
    ADD CONSTRAINT seguimiento_id_solicitud_fkey FOREIGN KEY (id_solicitud) REFERENCES public.solicitud(id_solicitud);


--
-- TOC entry 4945 (class 2606 OID 18318)
-- Name: seguimiento seguimiento_id_usuario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.seguimiento
    ADD CONSTRAINT seguimiento_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario);


--
-- TOC entry 4935 (class 2606 OID 18036)
-- Name: solicitud solicitud_id_cliente_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitud
    ADD CONSTRAINT solicitud_id_cliente_fkey FOREIGN KEY (id_cliente) REFERENCES public.cliente(id_cliente);


--
-- TOC entry 4936 (class 2606 OID 18298)
-- Name: solicitud solicitud_id_garantia_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitud
    ADD CONSTRAINT solicitud_id_garantia_fkey FOREIGN KEY (id_garantia) REFERENCES public.garantia(id_garantia);


--
-- TOC entry 4937 (class 2606 OID 18048)
-- Name: solicitud solicitud_id_producto_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitud
    ADD CONSTRAINT solicitud_id_producto_fkey FOREIGN KEY (id_producto) REFERENCES public.producto(id_producto);


--
-- TOC entry 4938 (class 2606 OID 18329)
-- Name: usuario usuario_id_rol_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_id_rol_fkey FOREIGN KEY (id_rol) REFERENCES public.rol(id_rol);


--
-- TOC entry 4939 (class 2606 OID 18339)
-- Name: usuario usuario_id_servicio_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_id_servicio_fkey FOREIGN KEY (id_servicio) REFERENCES public.servicio_autorizado(id_servicio);


-- Completed on 2026-09-19 00:22:18

--
-- PostgreSQL database dump complete
--

\unrestrict vfjIIb93aycyW4mWVDfXuz8IujEJqouMsyJcTXqIypOLmnH1BS4MSe9XbnScsYW

