--
-- PostgreSQL database dump
--

\restrict JgEzzLy2T6QcHADsD0h3zhd5Pqi1Qgcj6hgswarUDZGd4jvyYBnbynb4n5P0LEx

-- Dumped from database version 18.6
-- Dumped by pg_dump version 18.6

-- Started on 2026-09-28 19:58:00

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

--
-- TOC entry 2 (class 3079 OID 26676)
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;


--
-- TOC entry 5177 (class 0 OID 0)
-- Dependencies: 2
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 236 (class 1259 OID 18351)
-- Name: articulo; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.articulo (
    id_articulo bigint NOT NULL,
    nombre character varying(100) NOT NULL,
    descripcion character varying(255),
    categoria character varying(50),
    precio numeric(10,2) NOT NULL,
    stock integer DEFAULT 0 NOT NULL,
    estado character varying(30) DEFAULT 'ACTIVO'::character varying NOT NULL,
    marca character varying(50),
    modelo character varying(50),
    CONSTRAINT articulo_precio_check CHECK ((precio >= (0)::numeric)),
    CONSTRAINT articulo_stock_check CHECK ((stock >= 0))
);


ALTER TABLE public.articulo OWNER TO postgres;

--
-- TOC entry 241 (class 1259 OID 18484)
-- Name: articulo_id_articulo_seq1; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.articulo ALTER COLUMN id_articulo ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.articulo_id_articulo_seq1
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 233 (class 1259 OID 18127)
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
-- TOC entry 232 (class 1259 OID 18126)
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
-- TOC entry 5178 (class 0 OID 0)
-- Dependencies: 232
-- Name: asignacion_id_asignacion_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.asignacion_id_asignacion_seq OWNED BY public.asignacion.id_asignacion;


--
-- TOC entry 223 (class 1259 OID 17946)
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
-- TOC entry 222 (class 1259 OID 17945)
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
-- TOC entry 5179 (class 0 OID 0)
-- Dependencies: 222
-- Name: cliente_id_cliente_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.cliente_id_cliente_seq OWNED BY public.cliente.id_cliente;


--
-- TOC entry 240 (class 1259 OID 18391)
-- Name: detalle_venta; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.detalle_venta (
    id_detalle bigint NOT NULL,
    id_venta bigint NOT NULL,
    id_articulo bigint NOT NULL,
    cantidad integer NOT NULL,
    precio_unitario numeric(10,2) NOT NULL,
    subtotal numeric(12,2) NOT NULL,
    CONSTRAINT detalle_venta_cantidad_check CHECK ((cantidad > 0))
);


ALTER TABLE public.detalle_venta OWNER TO postgres;

--
-- TOC entry 239 (class 1259 OID 18390)
-- Name: detalle_venta_id_detalle_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.detalle_venta_id_detalle_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.detalle_venta_id_detalle_seq OWNER TO postgres;

--
-- TOC entry 5180 (class 0 OID 0)
-- Dependencies: 239
-- Name: detalle_venta_id_detalle_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.detalle_venta_id_detalle_seq OWNED BY public.detalle_venta.id_detalle;


--
-- TOC entry 225 (class 1259 OID 17959)
-- Name: producto; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.producto (
    id_producto bigint NOT NULL,
    marca character varying(50) NOT NULL,
    modelo character varying(50) NOT NULL,
    nro_serie character varying(50) NOT NULL,
    tipo_producto character varying(50) NOT NULL,
    id_cliente bigint NOT NULL,
    fecha_venta date NOT NULL,
    id_venta bigint
);


ALTER TABLE public.producto OWNER TO postgres;

--
-- TOC entry 224 (class 1259 OID 17958)
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
-- TOC entry 5181 (class 0 OID 0)
-- Dependencies: 224
-- Name: producto_id_producto_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.producto_id_producto_seq OWNED BY public.producto.id_producto;


--
-- TOC entry 229 (class 1259 OID 18060)
-- Name: rol; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.rol (
    id_rol bigint NOT NULL,
    nombre character varying(50) NOT NULL
);


ALTER TABLE public.rol OWNER TO postgres;

--
-- TOC entry 228 (class 1259 OID 18059)
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
-- TOC entry 5182 (class 0 OID 0)
-- Dependencies: 228
-- Name: rol_id_rol_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.rol_id_rol_seq OWNED BY public.rol.id_rol;


--
-- TOC entry 235 (class 1259 OID 18148)
-- Name: seguimiento; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.seguimiento (
    id_seguimiento bigint NOT NULL,
    id_solicitud bigint NOT NULL,
    id_usuario bigint NOT NULL,
    fecha timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    estado character varying(30) NOT NULL,
    diagnostico text
);


ALTER TABLE public.seguimiento OWNER TO postgres;

--
-- TOC entry 234 (class 1259 OID 18147)
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
-- TOC entry 5183 (class 0 OID 0)
-- Dependencies: 234
-- Name: seguimiento_id_seguimiento_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.seguimiento_id_seguimiento_seq OWNED BY public.seguimiento.id_seguimiento;


--
-- TOC entry 227 (class 1259 OID 17973)
-- Name: solicitud; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.solicitud (
    id_solicitud bigint NOT NULL,
    id_cliente bigint NOT NULL,
    id_producto bigint NOT NULL,
    fecha timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    descripcion text NOT NULL,
    estado_actual character varying(30) DEFAULT 'RECIBIDA'::character varying NOT NULL,
    codigo_publico character varying(40)
);


ALTER TABLE public.solicitud OWNER TO postgres;

--
-- TOC entry 226 (class 1259 OID 17972)
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
-- TOC entry 5184 (class 0 OID 0)
-- Dependencies: 226
-- Name: solicitud_id_solicitud_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.solicitud_id_solicitud_seq OWNED BY public.solicitud.id_solicitud;


--
-- TOC entry 231 (class 1259 OID 18079)
-- Name: usuario; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.usuario (
    id_usuario bigint NOT NULL,
    id_rol bigint NOT NULL,
    nombre character varying(100) NOT NULL,
    correo character varying(100) NOT NULL,
    clave character varying(255) NOT NULL,
    estado character varying(30) DEFAULT 'ACTIVO'::character varying
);


ALTER TABLE public.usuario OWNER TO postgres;

--
-- TOC entry 230 (class 1259 OID 18078)
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
-- TOC entry 5185 (class 0 OID 0)
-- Dependencies: 230
-- Name: usuario_id_usuario_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.usuario_id_usuario_seq OWNED BY public.usuario.id_usuario;


--
-- TOC entry 238 (class 1259 OID 18367)
-- Name: venta; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.venta (
    id_venta bigint NOT NULL,
    id_vendedor bigint NOT NULL,
    id_cliente bigint,
    fecha timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    total numeric(12,2) DEFAULT 0 NOT NULL
);


ALTER TABLE public.venta OWNER TO postgres;

--
-- TOC entry 237 (class 1259 OID 18366)
-- Name: venta_id_venta_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.venta_id_venta_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.venta_id_venta_seq OWNER TO postgres;

--
-- TOC entry 5186 (class 0 OID 0)
-- Dependencies: 237
-- Name: venta_id_venta_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.venta_id_venta_seq OWNED BY public.venta.id_venta;


--
-- TOC entry 4949 (class 2604 OID 18177)
-- Name: asignacion id_asignacion; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asignacion ALTER COLUMN id_asignacion SET DEFAULT nextval('public.asignacion_id_asignacion_seq'::regclass);


--
-- TOC entry 4941 (class 2604 OID 17998)
-- Name: cliente id_cliente; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cliente ALTER COLUMN id_cliente SET DEFAULT nextval('public.cliente_id_cliente_seq'::regclass);


--
-- TOC entry 4958 (class 2604 OID 18394)
-- Name: detalle_venta id_detalle; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_venta ALTER COLUMN id_detalle SET DEFAULT nextval('public.detalle_venta_id_detalle_seq'::regclass);


--
-- TOC entry 4942 (class 2604 OID 18011)
-- Name: producto id_producto; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.producto ALTER COLUMN id_producto SET DEFAULT nextval('public.producto_id_producto_seq'::regclass);


--
-- TOC entry 4946 (class 2604 OID 18238)
-- Name: rol id_rol; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rol ALTER COLUMN id_rol SET DEFAULT nextval('public.rol_id_rol_seq'::regclass);


--
-- TOC entry 4951 (class 2604 OID 18251)
-- Name: seguimiento id_seguimiento; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.seguimiento ALTER COLUMN id_seguimiento SET DEFAULT nextval('public.seguimiento_id_seguimiento_seq'::regclass);


--
-- TOC entry 4943 (class 2604 OID 18025)
-- Name: solicitud id_solicitud; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitud ALTER COLUMN id_solicitud SET DEFAULT nextval('public.solicitud_id_solicitud_seq'::regclass);


--
-- TOC entry 4947 (class 2604 OID 18309)
-- Name: usuario id_usuario; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario ALTER COLUMN id_usuario SET DEFAULT nextval('public.usuario_id_usuario_seq'::regclass);


--
-- TOC entry 4955 (class 2604 OID 18370)
-- Name: venta id_venta; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.venta ALTER COLUMN id_venta SET DEFAULT nextval('public.venta_id_venta_seq'::regclass);


--
-- TOC entry 5166 (class 0 OID 18351)
-- Dependencies: 236
-- Data for Name: articulo; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.articulo (id_articulo, nombre, descripcion, categoria, precio, stock, estado, marca, modelo) FROM stdin;
1	Heladera	Smart Fridge	Heladera	2750000.00	27	ACTIVO	Samsung	Smart Fridge
52	Midea Inverter 12000 BTU	Midea Inverter 12000 BTU - N/S SN-AA-0001	Aire acondicionado	4620000.00	0	INACTIVO	Midea	Inverter 12000 BTU
53	Samsung Monitor Odyssey 24	Samsung Monitor Odyssey 24 - N/S SN-MN-0001	Monitor	1200000.00	0	INACTIVO	Samsung	Monitor Odyssey 24
8	Televisor LG UR7800 43	LG UR7800 43 - N/S SN-TV-0002	Televisor	2460000.00	0	INACTIVO	LG	UR7800 43
9	Heladera Consul Cycle Defrost 340L	Consul Cycle Defrost 340L - N/S SN-HE-0007	Heladera	6110000.00	0	INACTIVO	Consul	Cycle Defrost 340L
10	Tablet Samsung Galaxy Tab A9	Samsung Galaxy Tab A9 - N/S SN-TB-0001	Tablet	1890000.00	0	INACTIVO	Samsung	Galaxy Tab A9
11	Tokyo Ventilador de techo	Tokyo Ventilador de techo - N/S SN-VE-0002	Ventilador	760000.00	0	INACTIVO	Tokyo	Ventilador de techo
12	Lavarropas LG TurboWash 13kg	LG TurboWash 13kg - N/S SN-LA-0004	Lavarropas	4100000.00	0	INACTIVO	LG	TurboWash 13kg
13	Freidora de aire Midea Air Fryer 5L	Midea Air Fryer 5L - N/S SN-FR-0003	Freidora de aire	670000.00	0	INACTIVO	Midea	Air Fryer 5L
14	Heladera Electrolux Frost Free 390L	Electrolux Frost Free 390L - N/S SN-HE-0006	Heladera	7900000.00	0	INACTIVO	Electrolux	Frost Free 390L
15	Celular Apple iPhone 13	Apple iPhone 13 - N/S SN-CE-0008	Celular	6280000.00	0	INACTIVO	Apple	iPhone 13
16	Liliana Ventilador de pie 20"	Liliana Ventilador de pie 20" - N/S SN-VE-0001	Ventilador	450000.00	0	INACTIVO	Liliana	Ventilador de pie 20"
17	Lavarropas Samsung EcoBubble 11kg	Samsung EcoBubble 11kg - N/S SN-LA-0003	Lavarropas	2700000.00	0	INACTIVO	Samsung	EcoBubble 11kg
18	Philips Airfryer 3000 Series	Philips Airfryer 3000 Series - N/S SN-FR-0002	Freidora de aire	530000.00	0	INACTIVO	Philips	Airfryer 3000 Series
19	LG Dual Inverter 18000 BTU	LG Dual Inverter 18000 BTU - N/S SN-AA-0002	Aire acondicionado	5490000.00	0	INACTIVO	LG	Dual Inverter 18000 BTU
20	Heladera LG Door in Door 601L	LG Door in Door 601L - N/S SN-HE-0005	Heladera	9660000.00	0	INACTIVO	LG	Door in Door 601L
21	Celular Motorola Edge 40	Motorola Edge 40 - N/S SN-CE-0007	Celular	4880000.00	0	INACTIVO	Motorola	Edge 40
22	Rheem Termotanque 50L	Rheem Termotanque 50L - N/S SN-CA-0002	Termotanque	1950000.00	0	INACTIVO	Rheem	Termotanque 50L
23	Lavarropas Electrolux EcoWash 8kg	Electrolux EcoWash 8kg - N/S SN-LA-0002	Lavarropas	3240000.00	0	INACTIVO	Electrolux	EcoWash 8kg
24	Impresora Brother DCP-T520W	Brother DCP-T520W - N/S SN-IM-0003	Impresora	1480000.00	0	INACTIVO	Brother	DCP-T520W
25	Heladera Samsung Side by Side 617L	Samsung Side by Side 617L - N/S SN-HE-0004	Heladera	6320000.00	0	INACTIVO	Samsung	Side by Side 617L
26	Celular Motorola Moto G84	Motorola Moto G84 - N/S SN-CE-0006	Celular	8850000.00	0	INACTIVO	Motorola	Moto G84
27	Tokyo Calefón 80L	Tokyo Calefón 80L - N/S SN-CA-0001	Calefón	1740000.00	0	INACTIVO	Tokyo	Calefón 80L
28	NGO Climatizador B1	NGO Climatizador B1 - N/S SN-CL-0003	Equipo de climatizacion	4290000.00	0	INACTIVO	NGO	Climatizador B1
29	Impresora Epson EcoTank L3250	Epson EcoTank L3250 - N/S SN-IM-0002	Impresora	1860000.00	0	INACTIVO	Epson	EcoTank L3250
30	Heladera Whirlpool Frost Free 430L	Whirlpool Frost Free 430L - N/S SN-HE-0003	Heladera	8640000.00	0	INACTIVO	Whirlpool	Frost Free 430L
31	Celular Xiaomi Poco X6 Pro	Xiaomi Poco X6 Pro - N/S SN-CE-0005	Celular	6700000.00	0	INACTIVO	Xiaomi	Poco X6 Pro
32	Black+Decker Batidora de mano 5 vel	Black+Decker Batidora de mano 5 vel - N/S SN-BA-0001	Batidora	190000.00	0	INACTIVO	Black+Decker	Batidora de mano 5 vel
33	NGO Climatizador A2	NGO Climatizador A2 - N/S SN-CL-0002	Equipo de climatizacion	5120000.00	0	INACTIVO	NGO	Climatizador A2
34	Impresora HP DeskJet Ink 2374	HP DeskJet Ink 2374 - N/S SN-IM-0001	Impresora	1890000.00	0	INACTIVO	HP	DeskJet Ink 2374
35	Heladera Samsung Twin Cooling 380L	Samsung Twin Cooling 380L - N/S SN-HE-0002	Heladera	3050000.00	0	INACTIVO	Samsung	Twin Cooling 380L
36	Televisor Aiwa AW32B4SM 32	Aiwa AW32B4SM 32 - N/S SN-TV-0012	Televisor	3910000.00	0	INACTIVO	Aiwa	AW32B4SM 32
37	Celular Xiaomi Redmi Note 13	Xiaomi Redmi Note 13 - N/S SN-CE-0004	Celular	1500000.00	0	INACTIVO	Xiaomi	Redmi Note 13
38	Philips Licuadora ProBlend 3	Philips Licuadora ProBlend 3 - N/S SN-LI-0002	Licuadora	680000.00	0	INACTIVO	Philips	Licuadora ProBlend 3
39	Electrolux Ecoturbo 24000 BTU	Electrolux Ecoturbo 24000 BTU - N/S SN-AA-0009	Aire acondicionado	6240000.00	0	INACTIVO	Electrolux	Ecoturbo 24000 BTU
40	AOC Monitor 22B2H	AOC Monitor 22B2H - N/S SN-MN-0003	Monitor	1580000.00	0	INACTIVO	AOC	Monitor 22B2H
41	Televisor Tokyo Smart HD 32	Tokyo Smart HD 32 - N/S SN-TV-0011	Televisor	3210000.00	0	INACTIVO	Tokyo	Smart HD 32
42	Celular Samsung Galaxy S23	Samsung Galaxy S23 - N/S SN-CE-0003	Celular	1870000.00	0	INACTIVO	Samsung	Galaxy S23
43	Heladera Whirlpool Frost Free 375L	Whirlpool Frost Free 375L - N/S SN-HE-0001	Heladera	11420000.00	0	INACTIVO	Whirlpool	Frost Free 375L
44	Oster Licuadora Reversible	Oster Licuadora Reversible - N/S SN-LI-0001	Licuadora	310000.00	0	INACTIVO	Oster	Licuadora Reversible
45	Tokyo Split 18000 BTU	Tokyo Split 18000 BTU - N/S SN-AA-0008	Aire acondicionado	3660000.00	0	INACTIVO	Tokyo	Split 18000 BTU
46	Celular Samsung Galaxy A54	Samsung Galaxy A54 - N/S SN-CE-0001	Celular	8160000.00	0	INACTIVO	Samsung	Galaxy A54
47	LG Monitor UltraGear 27	LG Monitor UltraGear 27 - N/S SN-MN-0002	Monitor	2150000.00	0	INACTIVO	LG	Monitor UltraGear 27
48	Televisor Philips PUD7406 43	Philips PUD7406 43 - N/S SN-TV-0010	Televisor	5990000.00	0	INACTIVO	Philips	PUD7406 43
49	Celular Samsung Galaxy A34	Samsung Galaxy A34 - N/S SN-CE-0002	Celular	5870000.00	0	INACTIVO	Samsung	Galaxy A34
50	Oster Horno eléctrico 22L	Oster Horno eléctrico 22L - N/S SN-HO-0002	Horno eléctrico	500000.00	0	INACTIVO	Oster	Horno eléctrico 22L
51	Aire acondicionado Gree Amber 9000 BTU	Gree Amber 9000 BTU - N/S SN-AA-0007	Aire acondicionado	5480000.00	0	INACTIVO	Gree	Amber 9000 BTU
54	NGO Climatizador A1	NGO Climatizador A1 - N/S SN-00012345	Equipo de climatizacion	2790000.00	0	INACTIVO	NGO	Climatizador A1
55	Televisor Hisense A6K 50	Hisense A6K 50 - N/S SN-TV-0009	Televisor	6180000.00	0	INACTIVO	Hisense	A6K 50
56	Midea Lavavajillas 14 cubiertos	Midea Lavavajillas 14 cubiertos - N/S SN-LV-0002	Lavavajillas	3890000.00	0	INACTIVO	Midea	Lavavajillas 14 cubiertos
57	Tokyo Horno eléctrico 45L	Tokyo Horno eléctrico 45L - N/S SN-HO-0001	Horno eléctrico	1030000.00	0	INACTIVO	Tokyo	Horno eléctrico 45L
58	Samsung WindFree 12000 BTU	Samsung WindFree 12000 BTU - N/S SN-AA-0006	Aire acondicionado	2750000.00	0	INACTIVO	Samsung	WindFree 12000 BTU
59	Notebook Asus VivoBook 15 X1504	Asus VivoBook 15 X1504 - N/S SN-NB-0006	Notebook	5280000.00	0	INACTIVO	Asus	VivoBook 15 X1504
60	Televisor TCL C645 55	TCL C645 55 - N/S SN-TV-0008	Televisor	7640000.00	0	INACTIVO	TCL	C645 55
61	Televisor Samsung Crystal UHD 55	Samsung Crystal UHD 55 - N/S SN-TV-0001	Televisor	7600000.00	0	INACTIVO	Samsung	Crystal UHD 55
62	Samsung Lavavajillas 12 cubiertos	Samsung Lavavajillas 12 cubiertos - N/S SN-LV-0001	Lavavajillas	4310000.00	0	INACTIVO	Samsung	Lavavajillas 12 cubiertos
63	Consul Cocina 5 hornallas	Consul Cocina 5 hornallas - N/S SN-CO-0002	Cocina	3860000.00	0	INACTIVO	Consul	Cocina 5 hornallas
64	LG Dual Inverter 12000 BTU	LG Dual Inverter 12000 BTU - N/S SN-AA-0005	Aire acondicionado	5730000.00	0	INACTIVO	LG	Dual Inverter 12000 BTU
65	Notebook Acer Aspire 5 A515	Acer Aspire 5 A515 - N/S SN-NB-0005	Notebook	5220000.00	0	INACTIVO	Acer	Aspire 5 A515
66	Televisor TCL P735 43	TCL P735 43 - N/S SN-TV-0007	Televisor	7290000.00	0	INACTIVO	TCL	P735 43
67	Electrolux Secarropas 8kg	Electrolux Secarropas 8kg - N/S SN-SE-0002	Secarropas	2400000.00	0	INACTIVO	Electrolux	Secarropas 8kg
68	Whirlpool Cocina 4 hornallas	Whirlpool Cocina 4 hornallas - N/S SN-CO-0001	Cocina	1770000.00	0	INACTIVO	Whirlpool	Cocina 4 hornallas
70	Midea Inverter 18000 BTU	Midea Inverter 18000 BTU - N/S SN-AA-0004	Aire acondicionado	4300000.00	0	INACTIVO	Midea	Inverter 18000 BTU
71	Notebook Dell Inspiron 15 3520	Dell Inspiron 15 3520 - N/S SN-NB-0004	Notebook	4390000.00	0	INACTIVO	Dell	Inspiron 15 3520
72	Televisor LG OLED C3 55	LG OLED C3 55 - N/S SN-TV-0006	Televisor	2630000.00	0	INACTIVO	LG	OLED C3 55
73	Whirlpool Secarropas 10kg	Whirlpool Secarropas 10kg - N/S SN-SE-0001	Secarropas	1290000.00	0	INACTIVO	Whirlpool	Secarropas 10kg
74	Electrolux Microondas 20L	Electrolux Microondas 20L - N/S SN-MO-0003	Microondas	800000.00	0	INACTIVO	Electrolux	Microondas 20L
75	Lavarropas Electrolux EcoWash 10kg	Electrolux EcoWash 10kg - N/S SN-LA-0001	Lavarropas	4940000.00	0	INACTIVO	Electrolux	EcoWash 10kg
77	Midea Inverter 9000 BTU	Midea Inverter 9000 BTU - N/S SN-AA-0003	Aire acondicionado	4860000.00	0	INACTIVO	Midea	Inverter 9000 BTU
78	Notebook HP Pavilion 15	HP Pavilion 15 - N/S SN-NB-0003	Notebook	4060000.00	0	INACTIVO	HP	Pavilion 15
79	Sony Barra de sonido HT-S400	Sony Barra de sonido HT-S400 - N/S SN-PA-0002	Barra de sonido	1450000.00	0	INACTIVO	Sony	Barra de sonido HT-S400
80	Televisor LG NanoCell NANO77 50	LG NanoCell NANO77 50 - N/S SN-TV-0005	Televisor	4210000.00	0	INACTIVO	LG	NanoCell NANO77 50
81	Lavarropas Midea Autoportante 9kg	Midea Autoportante 9kg - N/S SN-LA-0007	Lavarropas	2400000.00	0	INACTIVO	Midea	Autoportante 9kg
82	LG Microondas NeoChef 30L	LG Microondas NeoChef 30L - N/S SN-MO-0002	Microondas	1210000.00	0	INACTIVO	LG	Microondas NeoChef 30L
84	Heladera Patrick HPK151M 320L	Patrick HPK151M 320L - N/S SN-HE-0009	Heladera	2920000.00	0	INACTIVO	Patrick	HPK151M 320L
85	Notebook Lenovo IdeaPad Slim 3 14	Lenovo IdeaPad Slim 3 14 - N/S SN-NB-0002	Notebook	7600000.00	0	INACTIVO	Lenovo	IdeaPad Slim 3 14
86	JBL Parlante PartyBox 110	JBL Parlante PartyBox 110 - N/S SN-PA-0001	Parlante	740000.00	0	INACTIVO	JBL	Parlante PartyBox 110
87	Televisor Samsung QLED Q60C 50	Samsung QLED Q60C 50 - N/S SN-TV-0004	Televisor	5230000.00	0	INACTIVO	Samsung	QLED Q60C 50
88	Notebook Lenovo IdeaPad 3 15	Lenovo IdeaPad 3 15 - N/S SN-NB-0001	Notebook	4830000.00	0	INACTIVO	Lenovo	IdeaPad 3 15
89	Lavarropas Consul Facilite 11kg	Consul Facilite 11kg - N/S SN-LA-0006	Lavarropas	5380000.00	0	INACTIVO	Consul	Facilite 11kg
90	Samsung Microondas 32L	Samsung Microondas 32L - N/S SN-MO-0001	Microondas	1450000.00	0	INACTIVO	Samsung	Microondas 32L
91	Heladera Midea MDRB380 295L	Midea MDRB380 295L - N/S SN-HE-0008	Heladera	10310000.00	0	INACTIVO	Midea	MDRB380 295L
92	Tablet Lenovo Tab M10 Plus	Lenovo Tab M10 Plus - N/S SN-TB-0002	Tablet	2030000.00	0	INACTIVO	Lenovo	Tab M10 Plus
93	Philips Plancha a vapor 2400W	Philips Plancha a vapor 2400W - N/S SN-PL-0001	Plancha	360000.00	0	INACTIVO	Philips	Plancha a vapor 2400W
94	Televisor Samsung Crystal UHD 65	Samsung Crystal UHD 65 - N/S SN-TV-0003	Televisor	6670000.00	0	INACTIVO	Samsung	Crystal UHD 65
95	Freidora de aire Philips Airfryer XL	Philips Airfryer XL - N/S SN-FR-0001	Freidora de aire	860000.00	0	INACTIVO	Philips	Airfryer XL
96	Whirlpool Carga Superior 12kg	Whirlpool Carga Superior 12kg - N/S SN-LA-0005	Lavarropas	2010000.00	0	INACTIVO	Whirlpool	Carga Superior 12kg
97	Oster Air Fryer Digital 4L	Oster Air Fryer Digital 4L - N/S SN-FR-0004	Freidora de aire	960000.00	0	INACTIVO	Oster	Air Fryer Digital 4L
3	Heladera No Frost 300L	Color gris, eficiencia A	Heladera	4800000.00	5	ACTIVO	Consul	No Frost 300L
6	Microondas 20L	Digital, grill	Microondas	850000.00	15	ACTIVO	Electrolux	20L
2	Aire acondicionado 12000 BTU	Split frío/calor, 220V	Aire acondicionado	3200000.00	10	ACTIVO	Midea	12000 BTU
4	Lavarropas automático 8kg	Carga frontal	Lavarropas	3600000.00	8	ACTIVO	LG	Automático 8kg
5	Televisor LED 43"	Smart TV, Full HD	Televisor	2400000.00	11	ACTIVO	Samsung	LED 43"
\.


--
-- TOC entry 5163 (class 0 OID 18127)
-- Dependencies: 233
-- Data for Name: asignacion; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.asignacion (id_asignacion, id_solicitud, id_usuario, fecha_asignacion) FROM stdin;
6	13	5	2026-09-28 18:05:28.317167
7	14	5	2026-09-28 18:07:07.589377
8	13	3	2026-09-28 18:07:10.308817
\.


--
-- TOC entry 5153 (class 0 OID 17946)
-- Dependencies: 223
-- Data for Name: cliente; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.cliente (id_cliente, nombre, documento, telefono, correo) FROM stdin;
1	Diego Rojas	5823232	0981222333	diego.rojas@example.com
2	María Medina	2822789	0991979584	maria.medina@example.com
4	Verónica Ayala	4406101	0982400900	veronica.ayala@example.com
5	Luis Vera	5754061	+595981123456	luis.vera@example.com
6	Juan Giménez	3299431	+595985567890	juan.gimenez@example.com
7	Gabriela Giménez	1153499	+595984456789	\N
8	Laura Ayala	5298668	+595982234567	laura.ayala@example.com
9	Carolina Báez	2567340	0983345678	carolina.baez@example.com
10	Juan Villalba	3916205	0994856271	juan.villalba@example.com
18	Miguel Arévalo	4027619	0994999555	miguel.arevalo@example.com
19	Richard Stevens	1000000	0994222333	riyar@gmail.com
\.


--
-- TOC entry 5170 (class 0 OID 18391)
-- Dependencies: 240
-- Data for Name: detalle_venta; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.detalle_venta (id_detalle, id_venta, id_articulo, cantidad, precio_unitario, subtotal) FROM stdin;
5	5	8	1	2460000.00	2460000.00
6	6	9	1	6110000.00	6110000.00
7	7	10	1	1890000.00	1890000.00
8	8	11	1	760000.00	760000.00
9	9	12	1	4100000.00	4100000.00
10	10	13	1	670000.00	670000.00
11	11	14	1	7900000.00	7900000.00
12	12	15	1	6280000.00	6280000.00
13	13	16	1	450000.00	450000.00
14	14	17	1	2700000.00	2700000.00
15	15	18	1	530000.00	530000.00
16	16	19	1	5490000.00	5490000.00
17	17	20	1	9660000.00	9660000.00
18	18	21	1	4880000.00	4880000.00
19	19	22	1	1950000.00	1950000.00
20	20	23	1	3240000.00	3240000.00
21	21	24	1	1480000.00	1480000.00
22	22	25	1	6320000.00	6320000.00
23	23	26	1	8850000.00	8850000.00
24	24	27	1	1740000.00	1740000.00
25	25	28	1	4290000.00	4290000.00
26	26	29	1	1860000.00	1860000.00
27	27	30	1	8640000.00	8640000.00
28	28	31	1	6700000.00	6700000.00
29	29	32	1	190000.00	190000.00
30	30	33	1	5120000.00	5120000.00
31	31	34	1	1890000.00	1890000.00
32	32	35	1	3050000.00	3050000.00
33	33	36	1	3910000.00	3910000.00
34	34	37	1	1500000.00	1500000.00
35	35	38	1	680000.00	680000.00
36	36	39	1	6240000.00	6240000.00
37	37	40	1	1580000.00	1580000.00
38	38	41	1	3210000.00	3210000.00
39	39	42	1	1870000.00	1870000.00
40	40	43	1	11420000.00	11420000.00
41	41	44	1	310000.00	310000.00
42	42	45	1	3660000.00	3660000.00
43	43	46	1	8160000.00	8160000.00
44	44	47	1	2150000.00	2150000.00
45	45	48	1	5990000.00	5990000.00
46	46	49	1	5870000.00	5870000.00
47	47	50	1	500000.00	500000.00
48	48	51	1	5480000.00	5480000.00
49	49	52	1	4620000.00	4620000.00
50	50	53	1	1200000.00	1200000.00
51	51	54	1	2790000.00	2790000.00
52	52	55	1	6180000.00	6180000.00
53	53	56	1	3890000.00	3890000.00
54	54	57	1	1030000.00	1030000.00
55	55	58	1	2750000.00	2750000.00
56	56	59	1	5280000.00	5280000.00
57	57	60	1	7640000.00	7640000.00
58	58	61	1	7600000.00	7600000.00
59	59	62	1	4310000.00	4310000.00
60	60	63	1	3860000.00	3860000.00
61	61	64	1	5730000.00	5730000.00
62	62	65	1	5220000.00	5220000.00
63	63	66	1	7290000.00	7290000.00
64	64	67	1	2400000.00	2400000.00
65	65	68	1	1770000.00	1770000.00
67	67	70	1	4300000.00	4300000.00
68	68	71	1	4390000.00	4390000.00
69	69	72	1	2630000.00	2630000.00
70	70	73	1	1290000.00	1290000.00
71	71	74	1	800000.00	800000.00
72	72	75	1	4940000.00	4940000.00
74	74	77	1	4860000.00	4860000.00
75	75	78	1	4060000.00	4060000.00
76	76	79	1	1450000.00	1450000.00
77	77	80	1	4210000.00	4210000.00
78	78	81	1	2400000.00	2400000.00
79	79	82	1	1210000.00	1210000.00
81	81	84	1	2920000.00	2920000.00
82	82	85	1	7600000.00	7600000.00
83	83	86	1	740000.00	740000.00
84	84	87	1	5230000.00	5230000.00
85	85	88	1	4830000.00	4830000.00
86	86	89	1	5380000.00	5380000.00
87	87	90	1	1450000.00	1450000.00
88	88	91	1	10310000.00	10310000.00
89	89	92	1	2030000.00	2030000.00
90	90	93	1	360000.00	360000.00
91	91	94	1	6670000.00	6670000.00
92	92	95	1	860000.00	860000.00
93	93	96	1	2010000.00	2010000.00
94	94	97	1	960000.00	960000.00
3	95	1	1	2750000.00	2750000.00
4	96	5	1	2400000.00	2400000.00
\.


--
-- TOC entry 5155 (class 0 OID 17959)
-- Dependencies: 225
-- Data for Name: producto; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.producto (id_producto, marca, modelo, nro_serie, tipo_producto, id_cliente, fecha_venta, id_venta) FROM stdin;
72	Midea	Air Fryer 5L	SN-FR-0003	Freidora de aire	1	2025-05-03	10
86	Liliana	Ventilador de pie 20"	SN-VE-0001	Ventilador	7	2025-05-25	13
42	Samsung	EcoBubble 11kg	SN-LA-0003	Lavarropas	1	2025-06-02	14
71	Philips	Airfryer 3000 Series	SN-FR-0002	Freidora de aire	2	2025-06-09	15
10	LG	Dual Inverter 18000 BTU	SN-AA-0002	Aire acondicionado	5	2025-06-15	16
27	LG	Door in Door 601L	SN-HE-0005	Heladera	8	2025-06-17	17
56	Motorola	Edge 40	SN-CE-0007	Celular	7	2025-06-24	18
85	Rheem	Termotanque 50L	SN-CA-0002	Termotanque	6	2025-07-01	19
41	Electrolux	EcoWash 8kg	SN-LA-0002	Lavarropas	2	2025-07-09	20
70	Brother	DCP-T520W	SN-IM-0003	Impresora	1	2025-07-16	21
26	Samsung	Side by Side 617L	SN-HE-0004	Heladera	7	2025-07-24	22
55	Motorola	Moto G84	SN-CE-0006	Celular	6	2025-07-31	23
84	Tokyo	Calefón 80L	SN-CA-0001	Calefón	5	2025-08-07	24
40	NGO	Climatizador B1	SN-CL-0003	Equipo de climatizacion	1	2025-08-15	25
69	Epson	EcoTank L3250	SN-IM-0002	Impresora	10	2025-08-22	26
25	Whirlpool	Frost Free 430L	SN-HE-0003	Heladera	6	2025-08-30	27
54	Xiaomi	Poco X6 Pro	SN-CE-0005	Celular	5	2025-09-06	28
83	Black+Decker	Batidora de mano 5 vel	SN-BA-0001	Batidora	4	2025-09-13	29
39	NGO	Climatizador A2	SN-CL-0002	Equipo de climatizacion	10	2025-09-21	30
68	HP	DeskJet Ink 2374	SN-IM-0001	Impresora	9	2025-09-28	31
8	Samsung	Twin Cooling 380L	SN-HE-0002	Heladera	7	2025-10-01	32
24	Aiwa	AW32B4SM 32	SN-TV-0012	Televisor	5	2025-10-06	33
53	Xiaomi	Redmi Note 13	SN-CE-0004	Celular	4	2025-10-13	34
82	Philips	Licuadora ProBlend 3	SN-LI-0002	Licuadora	1	2025-10-20	35
38	Electrolux	Ecoturbo 24000 BTU	SN-AA-0009	Aire acondicionado	9	2025-10-28	36
67	AOC	Monitor 22B2H	SN-MN-0003	Monitor	8	2025-11-04	37
23	Tokyo	Smart HD 32	SN-TV-0011	Televisor	4	2025-11-12	38
52	Samsung	Galaxy S23	SN-CE-0003	Celular	1	2025-11-19	39
7	Whirlpool	Frost Free 375L	SN-HE-0001	Heladera	9	2025-11-20	40
81	Oster	Licuadora Reversible	SN-LI-0001	Licuadora	2	2025-11-26	41
37	Tokyo	Split 18000 BTU	SN-AA-0008	Aire acondicionado	8	2025-12-04	42
12	Samsung	Galaxy A54	SN-CE-0001	Celular	9	2025-12-05	43
66	LG	Monitor UltraGear 27	SN-MN-0002	Monitor	7	2025-12-11	44
22	Philips	PUD7406 43	SN-TV-0010	Televisor	1	2025-12-19	45
51	Samsung	Galaxy A34	SN-CE-0002	Celular	2	2025-12-26	46
80	Oster	Horno eléctrico 22L	SN-HO-0002	Horno eléctrico	1	2026-01-02	47
36	Gree	Amber 9000 BTU	SN-AA-0007	Aire acondicionado	7	2026-01-10	48
9	Midea	Inverter 12000 BTU	SN-AA-0001	Aire acondicionado	6	2026-01-15	49
65	Samsung	Monitor Odyssey 24	SN-MN-0001	Monitor	6	2026-01-17	50
1	NGO	Climatizador A1	SN-00012345	Equipo de climatizacion	1	2026-01-20	51
21	Hisense	A6K 50	SN-TV-0009	Televisor	2	2026-01-25	52
50	Midea	Lavavajillas 14 cubiertos	SN-LV-0002	Lavavajillas	1	2026-02-01	53
79	Tokyo	Horno eléctrico 45L	SN-HO-0001	Horno eléctrico	10	2026-02-08	54
64	Asus	VivoBook 15 X1504	SN-NB-0006	Notebook	5	2026-02-23	56
20	TCL	C645 55	SN-TV-0008	Televisor	1	2026-03-03	57
5	Samsung	Crystal UHD 55	SN-TV-0001	Televisor	5	2026-03-10	58
49	Samsung	Lavavajillas 12 cubiertos	SN-LV-0001	Lavavajillas	10	2026-03-10	59
78	Consul	Cocina 5 hornallas	SN-CO-0002	Cocina	9	2026-03-17	60
34	LG	Dual Inverter 12000 BTU	SN-AA-0005	Aire acondicionado	5	2026-03-25	61
19	TCL	P735 43	SN-TV-0007	Televisor	10	2026-04-09	63
48	Electrolux	Secarropas 8kg	SN-SE-0002	Secarropas	9	2026-04-16	64
77	Whirlpool	Cocina 4 hornallas	SN-CO-0001	Cocina	8	2026-04-23	65
33	Midea	Inverter 18000 BTU	SN-AA-0004	Aire acondicionado	4	2026-05-01	67
62	Dell	Inspiron 15 3520	SN-NB-0004	Notebook	1	2026-05-08	68
18	LG	OLED C3 55	SN-TV-0006	Televisor	9	2026-05-16	69
47	Whirlpool	Secarropas 10kg	SN-SE-0001	Secarropas	8	2026-05-23	70
76	Electrolux	Microondas 20L	SN-MO-0003	Microondas	7	2026-05-30	71
11	Electrolux	EcoWash 10kg	SN-LA-0001	Lavarropas	8	2026-05-30	72
32	Midea	Inverter 9000 BTU	SN-AA-0003	Aire acondicionado	1	2026-06-07	74
61	HP	Pavilion 15	SN-NB-0003	Notebook	2	2026-06-14	75
6	LG	UR7800 43	SN-TV-0002	Televisor	8	2024-05-02	5
29	Consul	Cycle Defrost 340L	SN-HE-0007	Heladera	10	2025-04-04	6
58	Samsung	Galaxy Tab A9	SN-TB-0001	Tablet	9	2025-04-11	7
87	Tokyo	Ventilador de techo	SN-VE-0002	Ventilador	8	2025-04-18	8
43	LG	TurboWash 13kg	SN-LA-0004	Lavarropas	4	2025-04-26	9
28	Electrolux	Frost Free 390L	SN-HE-0006	Heladera	9	2025-05-11	11
57	Apple	iPhone 13	SN-CE-0008	Celular	8	2025-05-18	12
35	Samsung	WindFree 12000 BTU	SN-AA-0006	Aire acondicionado	6	2026-02-16	55
63	Acer	Aspire 5 A515	SN-NB-0005	Notebook	4	2026-04-01	62
90	Sony	Barra de sonido HT-S400	SN-PA-0002	Barra de sonido	1	2026-06-21	76
17	LG	NanoCell NANO77 50	SN-TV-0005	Televisor	8	2026-06-22	77
46	Midea	Autoportante 9kg	SN-LA-0007	Lavarropas	7	2026-06-29	78
75	LG	Microondas NeoChef 30L	SN-MO-0002	Microondas	6	2026-07-06	79
31	Patrick	HPK151M 320L	SN-HE-0009	Heladera	2	2026-07-14	81
60	Lenovo	IdeaPad Slim 3 14	SN-NB-0002	Notebook	1	2026-07-21	82
89	JBL	Parlante PartyBox 110	SN-PA-0001	Parlante	10	2026-07-28	83
16	Samsung	QLED Q60C 50	SN-TV-0004	Televisor	7	2026-07-29	84
13	Lenovo	IdeaPad 3 15	SN-NB-0001	Notebook	6	2026-08-01	85
45	Consul	Facilite 11kg	SN-LA-0006	Lavarropas	6	2026-08-05	86
74	Samsung	Microondas 32L	SN-MO-0001	Microondas	5	2026-08-12	87
30	Midea	MDRB380 295L	SN-HE-0008	Heladera	1	2026-08-20	88
59	Lenovo	Tab M10 Plus	SN-TB-0002	Tablet	10	2026-08-27	89
88	Philips	Plancha a vapor 2400W	SN-PL-0001	Plancha	9	2026-09-03	90
15	Samsung	Crystal UHD 65	SN-TV-0003	Televisor	6	2026-09-04	91
14	Philips	Airfryer XL	SN-FR-0001	Freidora de aire	7	2026-09-10	92
44	Whirlpool	Carga Superior 12kg	SN-LA-0005	Lavarropas	5	2026-09-11	93
73	Oster	Air Fryer Digital 4L	SN-FR-0004	Freidora de aire	4	2026-09-18	94
91	Samsung	Smart Fridge	SN-HE-0010	Heladera	18	2026-09-23	95
92	Samsung	LED 43"	SN-TV-0013	Televisor	19	2026-09-25	96
\.


--
-- TOC entry 5159 (class 0 OID 18060)
-- Dependencies: 229
-- Data for Name: rol; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.rol (id_rol, nombre) FROM stdin;
1	ADMINISTRADOR
3	TECNICO
4	VENDEDOR
2	ATENCION
\.


--
-- TOC entry 5165 (class 0 OID 18148)
-- Dependencies: 235
-- Data for Name: seguimiento; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.seguimiento (id_seguimiento, id_solicitud, id_usuario, fecha, estado, diagnostico) FROM stdin;
\.


--
-- TOC entry 5157 (class 0 OID 17973)
-- Dependencies: 227
-- Data for Name: solicitud; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.solicitud (id_solicitud, id_cliente, id_producto, fecha, descripcion, estado_actual, codigo_publico) FROM stdin;
13	9	88	2026-09-28 18:05:18.497939	Enchufe dañanado	ASIGNADA	372297e5bfe245ee892a4ad178e68c3e
14	6	9	2026-09-28 18:06:16.148222	Sin gas	ASIGNADA	877ff44071ff4ee1abae13b85d562599
\.


--
-- TOC entry 5161 (class 0 OID 18079)
-- Dependencies: 231
-- Data for Name: usuario; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.usuario (id_usuario, id_rol, nombre, correo, clave, estado) FROM stdin;
1	1	Administrador Prueba	admin@ngosaeca.com.py	$2b$10$6AQbd.y2DZqc9SpI.IlTgeKMZsTMrvATD0UXfl46wVDiiIgiKRCWC	ACTIVO
3	3	Tecnico Prueba	tecnico@ngosaeca.com.py	$2b$10$DYIXKUvFECLaJ5nbV9c4Fu1Gj3IVmEpqyzEfQlvGuZjW1O5OrD9oS	ACTIVO
4	4	Vendedor Prueba	vendedor@ngosaeca.com.py	$2a$10$DkGiCCqdHz4M/QKT8a8NyepZBf1ocmaqM6DRXRzt0NtKCL/X0xw7G	ACTIVO
2	2	Atencion Prueba	atencion@ngosaeca.com.py	$2b$10$VQ7b.UWEvdcGTnZ9AhnjeOKCZFsLuzKAYHrLmW5WoByP.JeSxIqKq	ACTIVO
5	3	Tecnico Prueba 2	tecnico2@ngosaeca.com.py	$2a$10$do/BBAf1sF6w4dwO6Ikf9OxvMSInW48sCB4ptWEaYf9ywLQE6ahx.	ACTIVO
6	4	Carlos Benitez	carlos.benitez@ngosaeca.com.py	$2a$10$aZKlDqVU5RHQUN3cZA/UzuIheEINNltzFhMVD2zQxw0e79bcrSTJq	ACTIVO
7	4	Lucia Fernandez	lucia.fernandez@ngosaeca.com.py	$2a$10$9ZC8qN.PHttWQ968di8OWusPBsVG1csX1np1Our1tqs9DJxHE0gnq	ACTIVO
8	4	Marcos Duarte	marcos.duarte@ngosaeca.com.py	$2a$10$S6MFjqed1Iux/k2IIfu6sOmdD5vMqkGc9mjuoc4FBbjCf6Q13Loq2	ACTIVO
9	4	Sofia Acosta	sofia.acosta@ngosaeca.com.py	$2a$10$2nKdH6eQ1embZscEAmFAb.OmQUCr5el/zdx4R1orh1s5IJCZ/85cO	ACTIVO
10	4	Andres Cabrera	andres.cabrera@ngosaeca.com.py	$2a$10$8Iu1yEEDEYIWQOgt5WEdj.0J6STeGdyCyIOXUiE8sTT0pCRJ.HL.2	ACTIVO
\.


--
-- TOC entry 5168 (class 0 OID 18367)
-- Dependencies: 238
-- Data for Name: venta; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.venta (id_venta, id_vendedor, id_cliente, fecha, total) FROM stdin;
95	1	18	2026-09-23 01:31:36.828519	2750000.00
96	1	19	2026-09-25 17:25:55.112748	2400000.00
5	10	8	2024-05-02 16:03:27	2460000.00
6	8	10	2025-04-04 12:06:22	6110000.00
7	8	9	2025-04-11 09:32:07	1890000.00
8	6	8	2025-04-18 10:54:17	760000.00
9	7	4	2025-04-26 12:33:14	4100000.00
10	10	1	2025-05-03 13:16:34	670000.00
11	9	9	2025-05-11 11:33:54	7900000.00
12	7	8	2025-05-18 10:59:34	6280000.00
13	8	7	2025-05-25 09:13:15	450000.00
14	9	1	2025-06-02 15:25:49	2700000.00
15	10	2	2025-06-09 16:08:16	530000.00
16	7	5	2025-06-15 13:44:40	5490000.00
17	7	8	2025-06-17 14:16:39	9660000.00
18	9	7	2025-06-24 15:41:01	4880000.00
19	10	6	2025-07-01 11:08:49	1950000.00
20	8	2	2025-07-09 17:17:51	3240000.00
21	8	1	2025-07-16 17:57:44	1480000.00
22	7	7	2025-07-24 15:40:00	6320000.00
23	9	6	2025-07-31 16:18:01	8850000.00
24	7	5	2025-08-07 10:11:36	1740000.00
25	10	1	2025-08-15 11:52:02	4290000.00
26	10	10	2025-08-22 18:40:49	1860000.00
27	6	6	2025-08-30 11:28:14	8640000.00
28	10	5	2025-09-06 13:01:16	6700000.00
29	6	4	2025-09-13 10:46:05	190000.00
30	7	10	2025-09-21 13:17:27	5120000.00
31	9	9	2025-09-28 10:58:03	1890000.00
32	8	7	2025-10-01 14:51:51	3050000.00
33	10	5	2025-10-06 16:07:49	3910000.00
34	10	4	2025-10-13 18:11:26	1500000.00
35	7	1	2025-10-20 17:20:15	680000.00
36	10	9	2025-10-28 16:03:22	6240000.00
37	9	8	2025-11-04 13:09:53	1580000.00
38	6	4	2025-11-12 18:00:32	3210000.00
39	8	1	2025-11-19 15:04:13	1870000.00
40	6	9	2025-11-20 14:55:49	11420000.00
41	7	2	2025-11-26 17:46:59	310000.00
42	6	8	2025-12-04 12:08:42	3660000.00
43	9	9	2025-12-05 15:40:17	8160000.00
44	6	7	2025-12-11 10:57:10	2150000.00
45	10	1	2025-12-19 17:23:30	5990000.00
46	7	2	2025-12-26 11:22:00	5870000.00
47	10	1	2026-01-02 12:03:19	500000.00
48	8	7	2026-01-10 09:08:41	5480000.00
49	6	6	2026-01-15 12:23:15	4620000.00
50	8	6	2026-01-17 12:22:49	1200000.00
51	6	1	2026-01-20 09:43:22	2790000.00
52	10	2	2026-01-25 16:18:09	6180000.00
53	9	1	2026-02-01 15:48:20	3890000.00
54	8	10	2026-02-08 16:48:49	1030000.00
55	8	6	2026-02-16 11:54:38	2750000.00
56	8	5	2026-02-23 09:37:04	5280000.00
57	10	1	2026-03-03 18:34:55	7640000.00
58	7	5	2026-03-10 12:58:26	7600000.00
59	10	10	2026-03-10 16:05:03	4310000.00
60	6	9	2026-03-17 18:48:44	3860000.00
61	10	5	2026-03-25 15:16:03	5730000.00
62	10	4	2026-04-01 18:22:14	5220000.00
63	7	10	2026-04-09 16:00:46	7290000.00
64	6	9	2026-04-16 09:07:38	2400000.00
65	10	8	2026-04-23 14:49:03	1770000.00
67	9	4	2026-05-01 15:55:28	4300000.00
68	6	1	2026-05-08 18:03:51	4390000.00
69	7	9	2026-05-16 11:33:26	2630000.00
70	7	8	2026-05-23 13:05:23	1290000.00
71	9	7	2026-05-30 11:48:42	800000.00
72	7	8	2026-05-30 16:21:45	4940000.00
74	6	1	2026-06-07 09:05:25	4860000.00
75	7	2	2026-06-14 12:31:07	4060000.00
76	8	1	2026-06-21 13:16:34	1450000.00
77	8	8	2026-06-22 14:00:28	4210000.00
78	8	7	2026-06-29 15:38:03	2400000.00
79	6	6	2026-07-06 12:37:16	1210000.00
81	7	2	2026-07-14 16:45:38	2920000.00
82	6	1	2026-07-21 10:13:46	7600000.00
83	8	10	2026-07-28 16:23:57	740000.00
84	8	7	2026-07-29 11:24:28	5230000.00
85	8	6	2026-08-01 12:35:01	4830000.00
86	10	6	2026-08-05 10:02:50	5380000.00
87	9	5	2026-08-12 14:31:50	1450000.00
88	9	1	2026-08-20 10:58:27	10310000.00
89	6	10	2026-08-27 11:13:07	2030000.00
90	8	9	2026-09-03 11:10:05	360000.00
91	7	6	2026-09-04 15:27:29	6670000.00
92	6	7	2026-09-10 15:25:22	860000.00
93	6	5	2026-09-11 18:47:26	2010000.00
94	10	4	2026-09-18 16:35:14	960000.00
\.


--
-- TOC entry 5187 (class 0 OID 0)
-- Dependencies: 241
-- Name: articulo_id_articulo_seq1; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.articulo_id_articulo_seq1', 97, true);


--
-- TOC entry 5188 (class 0 OID 0)
-- Dependencies: 232
-- Name: asignacion_id_asignacion_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.asignacion_id_asignacion_seq', 8, true);


--
-- TOC entry 5189 (class 0 OID 0)
-- Dependencies: 222
-- Name: cliente_id_cliente_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.cliente_id_cliente_seq', 19, true);


--
-- TOC entry 5190 (class 0 OID 0)
-- Dependencies: 239
-- Name: detalle_venta_id_detalle_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.detalle_venta_id_detalle_seq', 94, true);


--
-- TOC entry 5191 (class 0 OID 0)
-- Dependencies: 224
-- Name: producto_id_producto_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.producto_id_producto_seq', 92, true);


--
-- TOC entry 5192 (class 0 OID 0)
-- Dependencies: 228
-- Name: rol_id_rol_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.rol_id_rol_seq', 4, true);


--
-- TOC entry 5193 (class 0 OID 0)
-- Dependencies: 234
-- Name: seguimiento_id_seguimiento_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.seguimiento_id_seguimiento_seq', 8, true);


--
-- TOC entry 5194 (class 0 OID 0)
-- Dependencies: 226
-- Name: solicitud_id_solicitud_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.solicitud_id_solicitud_seq', 14, true);


--
-- TOC entry 5195 (class 0 OID 0)
-- Dependencies: 230
-- Name: usuario_id_usuario_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.usuario_id_usuario_seq', 10, true);


--
-- TOC entry 5196 (class 0 OID 0)
-- Dependencies: 237
-- Name: venta_id_venta_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.venta_id_venta_seq', 96, true);


--
-- TOC entry 4987 (class 2606 OID 18365)
-- Name: articulo articulo_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.articulo
    ADD CONSTRAINT articulo_pkey PRIMARY KEY (id_articulo);


--
-- TOC entry 4983 (class 2606 OID 18179)
-- Name: asignacion asignacion_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asignacion
    ADD CONSTRAINT asignacion_pkey PRIMARY KEY (id_asignacion);


--
-- TOC entry 4965 (class 2606 OID 18447)
-- Name: cliente cliente_documento_unique; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cliente
    ADD CONSTRAINT cliente_documento_unique UNIQUE (documento);


--
-- TOC entry 4967 (class 2606 OID 18000)
-- Name: cliente cliente_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cliente
    ADD CONSTRAINT cliente_pkey PRIMARY KEY (id_cliente);


--
-- TOC entry 4991 (class 2606 OID 18403)
-- Name: detalle_venta detalle_venta_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_venta
    ADD CONSTRAINT detalle_venta_pkey PRIMARY KEY (id_detalle);


--
-- TOC entry 4969 (class 2606 OID 17971)
-- Name: producto producto_nro_serie_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.producto
    ADD CONSTRAINT producto_nro_serie_key UNIQUE (nro_serie);


--
-- TOC entry 4971 (class 2606 OID 18013)
-- Name: producto producto_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.producto
    ADD CONSTRAINT producto_pkey PRIMARY KEY (id_producto);


--
-- TOC entry 4977 (class 2606 OID 18240)
-- Name: rol rol_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rol
    ADD CONSTRAINT rol_pkey PRIMARY KEY (id_rol);


--
-- TOC entry 4985 (class 2606 OID 18253)
-- Name: seguimiento seguimiento_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.seguimiento
    ADD CONSTRAINT seguimiento_pkey PRIMARY KEY (id_seguimiento);


--
-- TOC entry 4973 (class 2606 OID 18486)
-- Name: solicitud solicitud_codigo_publico_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitud
    ADD CONSTRAINT solicitud_codigo_publico_key UNIQUE (codigo_publico);


--
-- TOC entry 4975 (class 2606 OID 18027)
-- Name: solicitud solicitud_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitud
    ADD CONSTRAINT solicitud_pkey PRIMARY KEY (id_solicitud);


--
-- TOC entry 4979 (class 2606 OID 18092)
-- Name: usuario usuario_correo_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_correo_key UNIQUE (correo);


--
-- TOC entry 4981 (class 2606 OID 18311)
-- Name: usuario usuario_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_pkey PRIMARY KEY (id_usuario);


--
-- TOC entry 4989 (class 2606 OID 18379)
-- Name: venta venta_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.venta
    ADD CONSTRAINT venta_pkey PRIMARY KEY (id_venta);


--
-- TOC entry 4997 (class 2606 OID 18186)
-- Name: asignacion asignacion_id_solicitud_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asignacion
    ADD CONSTRAINT asignacion_id_solicitud_fkey FOREIGN KEY (id_solicitud) REFERENCES public.solicitud(id_solicitud);


--
-- TOC entry 4998 (class 2606 OID 18313)
-- Name: asignacion asignacion_id_usuario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asignacion
    ADD CONSTRAINT asignacion_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario);


--
-- TOC entry 5003 (class 2606 OID 18409)
-- Name: detalle_venta detalle_venta_id_articulo_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_venta
    ADD CONSTRAINT detalle_venta_id_articulo_fkey FOREIGN KEY (id_articulo) REFERENCES public.articulo(id_articulo);


--
-- TOC entry 5004 (class 2606 OID 18404)
-- Name: detalle_venta detalle_venta_id_venta_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_venta
    ADD CONSTRAINT detalle_venta_id_venta_fkey FOREIGN KEY (id_venta) REFERENCES public.venta(id_venta) ON DELETE CASCADE;


--
-- TOC entry 4992 (class 2606 OID 18416)
-- Name: producto producto_id_cliente_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.producto
    ADD CONSTRAINT producto_id_cliente_fkey FOREIGN KEY (id_cliente) REFERENCES public.cliente(id_cliente);


--
-- TOC entry 4993 (class 2606 OID 26717)
-- Name: producto producto_id_venta_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.producto
    ADD CONSTRAINT producto_id_venta_fkey FOREIGN KEY (id_venta) REFERENCES public.venta(id_venta);


--
-- TOC entry 4999 (class 2606 OID 18262)
-- Name: seguimiento seguimiento_id_solicitud_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.seguimiento
    ADD CONSTRAINT seguimiento_id_solicitud_fkey FOREIGN KEY (id_solicitud) REFERENCES public.solicitud(id_solicitud);


--
-- TOC entry 5000 (class 2606 OID 18318)
-- Name: seguimiento seguimiento_id_usuario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.seguimiento
    ADD CONSTRAINT seguimiento_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario);


--
-- TOC entry 4994 (class 2606 OID 18036)
-- Name: solicitud solicitud_id_cliente_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitud
    ADD CONSTRAINT solicitud_id_cliente_fkey FOREIGN KEY (id_cliente) REFERENCES public.cliente(id_cliente);


--
-- TOC entry 4995 (class 2606 OID 18048)
-- Name: solicitud solicitud_id_producto_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitud
    ADD CONSTRAINT solicitud_id_producto_fkey FOREIGN KEY (id_producto) REFERENCES public.producto(id_producto);


--
-- TOC entry 4996 (class 2606 OID 18329)
-- Name: usuario usuario_id_rol_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_id_rol_fkey FOREIGN KEY (id_rol) REFERENCES public.rol(id_rol);


--
-- TOC entry 5001 (class 2606 OID 18385)
-- Name: venta venta_id_cliente_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.venta
    ADD CONSTRAINT venta_id_cliente_fkey FOREIGN KEY (id_cliente) REFERENCES public.cliente(id_cliente);


--
-- TOC entry 5002 (class 2606 OID 18380)
-- Name: venta venta_id_vendedor_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.venta
    ADD CONSTRAINT venta_id_vendedor_fkey FOREIGN KEY (id_vendedor) REFERENCES public.usuario(id_usuario);


-- Completed on 2026-09-28 19:58:00

--
-- PostgreSQL database dump complete
--

\unrestrict JgEzzLy2T6QcHADsD0h3zhd5Pqi1Qgcj6hgswarUDZGd4jvyYBnbynb4n5P0LEx

