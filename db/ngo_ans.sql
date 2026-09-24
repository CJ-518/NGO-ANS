--
-- PostgreSQL database dump
--

\restrict jl6t5O6vXlONZNr2R0jTsD03PxSVF6USwiKtSbYmbVZhwsDlO11Ko6YxinV7OwM

-- Dumped from database version 18.6
-- Dumped by pg_dump version 18.6

-- Started on 2026-09-24 05:57:00

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
-- TOC entry 234 (class 1259 OID 18351)
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
    CONSTRAINT articulo_precio_check CHECK ((precio >= (0)::numeric)),
    CONSTRAINT articulo_stock_check CHECK ((stock >= 0))
);


ALTER TABLE public.articulo OWNER TO postgres;

--
-- TOC entry 233 (class 1259 OID 18350)
-- Name: articulo_id_articulo_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.articulo_id_articulo_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.articulo_id_articulo_seq OWNER TO postgres;

--
-- TOC entry 5156 (class 0 OID 0)
-- Dependencies: 233
-- Name: articulo_id_articulo_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.articulo_id_articulo_seq OWNED BY public.articulo.id_articulo;


--
-- TOC entry 230 (class 1259 OID 18127)
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
-- TOC entry 229 (class 1259 OID 18126)
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
-- TOC entry 5157 (class 0 OID 0)
-- Dependencies: 229
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
-- TOC entry 5158 (class 0 OID 0)
-- Dependencies: 219
-- Name: cliente_id_cliente_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.cliente_id_cliente_seq OWNED BY public.cliente.id_cliente;


--
-- TOC entry 238 (class 1259 OID 18391)
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
-- TOC entry 237 (class 1259 OID 18390)
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
-- TOC entry 5159 (class 0 OID 0)
-- Dependencies: 237
-- Name: detalle_venta_id_detalle_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.detalle_venta_id_detalle_seq OWNED BY public.detalle_venta.id_detalle;


--
-- TOC entry 240 (class 1259 OID 18455)
-- Name: garantia; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.garantia (
    id_garantia bigint NOT NULL,
    estado character varying(30) NOT NULL,
    fecha_fin date NOT NULL,
    fecha_inicio date NOT NULL,
    id_cliente bigint NOT NULL,
    id_producto bigint NOT NULL
);


ALTER TABLE public.garantia OWNER TO postgres;

--
-- TOC entry 239 (class 1259 OID 18454)
-- Name: garantia_id_garantia_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.garantia ALTER COLUMN id_garantia ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.garantia_id_garantia_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 222 (class 1259 OID 17959)
-- Name: producto; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.producto (
    id_producto bigint NOT NULL,
    marca character varying(50) NOT NULL,
    modelo character varying(50) NOT NULL,
    nro_serie character varying(50) NOT NULL,
    tipo_producto character varying(50) NOT NULL,
    id_cliente bigint NOT NULL,
    fecha_venta date NOT NULL
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
-- TOC entry 5160 (class 0 OID 0)
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
-- TOC entry 5161 (class 0 OID 0)
-- Dependencies: 225
-- Name: rol_id_rol_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.rol_id_rol_seq OWNED BY public.rol.id_rol;


--
-- TOC entry 232 (class 1259 OID 18148)
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
-- TOC entry 231 (class 1259 OID 18147)
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
-- TOC entry 5162 (class 0 OID 0)
-- Dependencies: 231
-- Name: seguimiento_id_seguimiento_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.seguimiento_id_seguimiento_seq OWNED BY public.seguimiento.id_seguimiento;


--
-- TOC entry 242 (class 1259 OID 18467)
-- Name: servicio_autorizado; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.servicio_autorizado (
    id_servicio bigint NOT NULL,
    ciudad character varying(100),
    estado character varying(30),
    nombre character varying(100) NOT NULL
);


ALTER TABLE public.servicio_autorizado OWNER TO postgres;

--
-- TOC entry 241 (class 1259 OID 18466)
-- Name: servicio_autorizado_id_servicio_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.servicio_autorizado ALTER COLUMN id_servicio ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.servicio_autorizado_id_servicio_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


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
    estado_actual character varying(30) DEFAULT 'RECIBIDA'::character varying NOT NULL
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
-- TOC entry 5163 (class 0 OID 0)
-- Dependencies: 223
-- Name: solicitud_id_solicitud_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.solicitud_id_solicitud_seq OWNED BY public.solicitud.id_solicitud;


--
-- TOC entry 228 (class 1259 OID 18079)
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
-- TOC entry 227 (class 1259 OID 18078)
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
-- TOC entry 5164 (class 0 OID 0)
-- Dependencies: 227
-- Name: usuario_id_usuario_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.usuario_id_usuario_seq OWNED BY public.usuario.id_usuario;


--
-- TOC entry 236 (class 1259 OID 18367)
-- Name: venta; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.venta (
    id_venta bigint NOT NULL,
    id_vendedor bigint NOT NULL,
    id_cliente bigint,
    fecha timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    total numeric(12,2) DEFAULT 0 NOT NULL,
    estado character varying(30) DEFAULT 'COMPLETADA'::character varying NOT NULL
);


ALTER TABLE public.venta OWNER TO postgres;

--
-- TOC entry 235 (class 1259 OID 18366)
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
-- TOC entry 5165 (class 0 OID 0)
-- Dependencies: 235
-- Name: venta_id_venta_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.venta_id_venta_seq OWNED BY public.venta.id_venta;


--
-- TOC entry 4923 (class 2604 OID 18354)
-- Name: articulo id_articulo; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.articulo ALTER COLUMN id_articulo SET DEFAULT nextval('public.articulo_id_articulo_seq'::regclass);


--
-- TOC entry 4919 (class 2604 OID 18177)
-- Name: asignacion id_asignacion; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asignacion ALTER COLUMN id_asignacion SET DEFAULT nextval('public.asignacion_id_asignacion_seq'::regclass);


--
-- TOC entry 4911 (class 2604 OID 17998)
-- Name: cliente id_cliente; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cliente ALTER COLUMN id_cliente SET DEFAULT nextval('public.cliente_id_cliente_seq'::regclass);


--
-- TOC entry 4930 (class 2604 OID 18394)
-- Name: detalle_venta id_detalle; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_venta ALTER COLUMN id_detalle SET DEFAULT nextval('public.detalle_venta_id_detalle_seq'::regclass);


--
-- TOC entry 4912 (class 2604 OID 18011)
-- Name: producto id_producto; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.producto ALTER COLUMN id_producto SET DEFAULT nextval('public.producto_id_producto_seq'::regclass);


--
-- TOC entry 4916 (class 2604 OID 18238)
-- Name: rol id_rol; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rol ALTER COLUMN id_rol SET DEFAULT nextval('public.rol_id_rol_seq'::regclass);


--
-- TOC entry 4921 (class 2604 OID 18251)
-- Name: seguimiento id_seguimiento; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.seguimiento ALTER COLUMN id_seguimiento SET DEFAULT nextval('public.seguimiento_id_seguimiento_seq'::regclass);


--
-- TOC entry 4913 (class 2604 OID 18025)
-- Name: solicitud id_solicitud; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitud ALTER COLUMN id_solicitud SET DEFAULT nextval('public.solicitud_id_solicitud_seq'::regclass);


--
-- TOC entry 4917 (class 2604 OID 18309)
-- Name: usuario id_usuario; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario ALTER COLUMN id_usuario SET DEFAULT nextval('public.usuario_id_usuario_seq'::regclass);


--
-- TOC entry 4926 (class 2604 OID 18370)
-- Name: venta id_venta; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.venta ALTER COLUMN id_venta SET DEFAULT nextval('public.venta_id_venta_seq'::regclass);


--
-- TOC entry 5142 (class 0 OID 18351)
-- Dependencies: 234
-- Data for Name: articulo; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.articulo (id_articulo, nombre, descripcion, categoria, precio, stock, estado) FROM stdin;
1	Heladera	Smart Fridge	Samsung	2750000.00	27	ACTIVO
\.


--
-- TOC entry 5138 (class 0 OID 18127)
-- Dependencies: 230
-- Data for Name: asignacion; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.asignacion (id_asignacion, id_solicitud, id_usuario, fecha_asignacion) FROM stdin;
5	11	3	2026-09-24 05:50:04.154817
\.


--
-- TOC entry 5128 (class 0 OID 17946)
-- Dependencies: 220
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
16	Natalia Villalba	4756146	0994777222	natalia.villalba@example.com
18	Miguel Arévalo	4027619	0994999555	miguel.arevalo@example.com
\.


--
-- TOC entry 5146 (class 0 OID 18391)
-- Dependencies: 238
-- Data for Name: detalle_venta; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.detalle_venta (id_detalle, id_venta, id_articulo, cantidad, precio_unitario, subtotal) FROM stdin;
3	3	1	1	2750000.00	2750000.00
\.


--
-- TOC entry 5148 (class 0 OID 18455)
-- Dependencies: 240
-- Data for Name: garantia; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.garantia (id_garantia, estado, fecha_fin, fecha_inicio, id_cliente, id_producto) FROM stdin;
\.


--
-- TOC entry 5130 (class 0 OID 17959)
-- Dependencies: 222
-- Data for Name: producto; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.producto (id_producto, marca, modelo, nro_serie, tipo_producto, id_cliente, fecha_venta) FROM stdin;
82	Philips	Licuadora ProBlend 3	SN-LI-0002	Licuadora	1	2025-10-20
72	Midea	Air Fryer 5L	SN-FR-0003	Freidora de aire	1	2025-05-03
52	Samsung	Galaxy S23	SN-CE-0003	Celular	1	2025-11-19
32	Midea	Inverter 9000 BTU	SN-AA-0003	Aire acondicionado	1	2026-06-07
62	Dell	Inspiron 15 3520	SN-NB-0004	Notebook	1	2026-05-08
42	Samsung	EcoBubble 11kg	SN-LA-0003	Lavarropas	1	2025-06-02
22	Philips	PUD7406 43	SN-TV-0010	Televisor	1	2025-12-19
11	Electrolux	EcoWash 10kg	SN-LA-0001	Lavarropas	8	2026-05-30
12	Samsung	Galaxy A54	SN-CE-0001	Celular	9	2025-12-05
10	LG	Dual Inverter 18000 BTU	SN-AA-0002	Aire acondicionado	5	2025-06-15
13	Lenovo	IdeaPad 3 15	SN-NB-0001	Notebook	6	2026-08-01
5	Samsung	Crystal UHD 55	SN-TV-0001	Televisor	5	2026-03-10
8	Samsung	Twin Cooling 380L	SN-HE-0002	Heladera	7	2025-10-01
6	LG	UR7800 43	SN-TV-0002	Televisor	8	2024-05-02
1	NGO	Climatizador A1	SN-00012345	Equipo de climatizacion	1	2026-01-20
14	Philips	Airfryer XL	SN-FR-0001	Freidora de aire	7	2026-09-10
9	Midea	Inverter 12000 BTU	SN-AA-0001	Aire acondicionado	6	2026-01-15
7	Whirlpool	Frost Free 375L	SN-HE-0001	Heladera	9	2025-11-20
20	TCL	C645 55	SN-TV-0008	Televisor	1	2026-03-03
25	Whirlpool	Frost Free 430L	SN-HE-0003	Heladera	6	2025-08-30
26	Samsung	Side by Side 617L	SN-HE-0004	Heladera	7	2025-07-24
27	LG	Door in Door 601L	SN-HE-0005	Heladera	8	2025-06-17
39	NGO	Climatizador A2	SN-CL-0002	Equipo de climatizacion	10	2025-09-21
17	LG	NanoCell NANO77 50	SN-TV-0005	Televisor	8	2026-06-22
66	LG	Monitor UltraGear 27	SN-MN-0002	Monitor	7	2025-12-11
89	JBL	Parlante PartyBox 110	SN-PA-0001	Parlante	10	2026-07-28
33	Midea	Inverter 18000 BTU	SN-AA-0004	Aire acondicionado	4	2026-05-01
57	Apple	iPhone 13	SN-CE-0008	Celular	8	2025-05-18
31	Patrick	HPK151M 320L	SN-HE-0009	Heladera	2	2026-07-14
34	LG	Dual Inverter 12000 BTU	SN-AA-0005	Aire acondicionado	5	2026-03-25
18	LG	OLED C3 55	SN-TV-0006	Televisor	9	2026-05-16
64	Asus	VivoBook 15 X1504	SN-NB-0006	Notebook	5	2026-02-23
71	Philips	Airfryer 3000 Series	SN-FR-0002	Freidora de aire	2	2025-06-09
47	Whirlpool	Secarropas 10kg	SN-SE-0001	Secarropas	8	2026-05-23
46	Midea	Autoportante 9kg	SN-LA-0007	Lavarropas	7	2026-06-29
83	Black+Decker	Batidora de mano 5 vel	SN-BA-0001	Batidora	4	2025-09-13
15	Samsung	Crystal UHD 65	SN-TV-0003	Televisor	6	2026-09-04
77	Whirlpool	Cocina 4 hornallas	SN-CO-0001	Cocina	8	2026-04-23
73	Oster	Air Fryer Digital 4L	SN-FR-0004	Freidora de aire	4	2026-09-18
56	Motorola	Edge 40	SN-CE-0007	Celular	7	2025-06-24
40	NGO	Climatizador B1	SN-CL-0003	Equipo de climatizacion	1	2025-08-15
21	Hisense	A6K 50	SN-TV-0009	Televisor	2	2026-01-25
19	TCL	P735 43	SN-TV-0007	Televisor	10	2026-04-09
65	Samsung	Monitor Odyssey 24	SN-MN-0001	Monitor	6	2026-01-17
37	Tokyo	Split 18000 BTU	SN-AA-0008	Aire acondicionado	8	2025-12-04
85	Rheem	Termotanque 50L	SN-CA-0002	Termotanque	6	2025-07-01
78	Consul	Cocina 5 hornallas	SN-CO-0002	Cocina	9	2026-03-17
24	Aiwa	AW32B4SM 32	SN-TV-0012	Televisor	5	2025-10-06
55	Motorola	Moto G84	SN-CE-0006	Celular	6	2025-07-31
68	HP	DeskJet Ink 2374	SN-IM-0001	Impresora	9	2025-09-28
38	Electrolux	Ecoturbo 24000 BTU	SN-AA-0009	Aire acondicionado	9	2025-10-28
80	Oster	Horno eléctrico 22L	SN-HO-0002	Horno eléctrico	1	2026-01-02
48	Electrolux	Secarropas 8kg	SN-SE-0002	Secarropas	9	2026-04-16
28	Electrolux	Frost Free 390L	SN-HE-0006	Heladera	9	2025-05-11
30	Midea	MDRB380 295L	SN-HE-0008	Heladera	1	2026-08-20
67	AOC	Monitor 22B2H	SN-MN-0003	Monitor	8	2025-11-04
50	Midea	Lavavajillas 14 cubiertos	SN-LV-0002	Lavavajillas	1	2026-02-01
51	Samsung	Galaxy A34	SN-CE-0002	Celular	2	2025-12-26
76	Electrolux	Microondas 20L	SN-MO-0003	Microondas	7	2026-05-30
69	Epson	EcoTank L3250	SN-IM-0002	Impresora	10	2025-08-22
81	Oster	Licuadora Reversible	SN-LI-0001	Licuadora	2	2025-11-26
79	Tokyo	Horno eléctrico 45L	SN-HO-0001	Horno eléctrico	10	2026-02-08
90	Sony	Barra de sonido HT-S400	SN-PA-0002	Barra de sonido	1	2026-06-21
59	Lenovo	Tab M10 Plus	SN-TB-0002	Tablet	10	2026-08-27
84	Tokyo	Calefón 80L	SN-CA-0001	Calefón	5	2025-08-07
74	Samsung	Microondas 32L	SN-MO-0001	Microondas	5	2026-08-12
29	Consul	Cycle Defrost 340L	SN-HE-0007	Heladera	10	2025-04-04
41	Electrolux	EcoWash 8kg	SN-LA-0002	Lavarropas	2	2025-07-09
16	Samsung	QLED Q60C 50	SN-TV-0004	Televisor	7	2026-07-29
54	Xiaomi	Poco X6 Pro	SN-CE-0005	Celular	5	2025-09-06
36	Gree	Amber 9000 BTU	SN-AA-0007	Aire acondicionado	7	2026-01-10
4	Samsung	7894516	45689186	Vibrador	5	2026-04-24
53	Xiaomi	Redmi Note 13	SN-CE-0004	Celular	4	2025-10-13
23	Tokyo	Smart HD 32	SN-TV-0011	Televisor	4	2025-11-12
44	Whirlpool	Carga Superior 12kg	SN-LA-0005	Lavarropas	5	2026-09-11
58	Samsung	Galaxy Tab A9	SN-TB-0001	Tablet	9	2025-04-11
86	Liliana	Ventilador de pie 20"	SN-VE-0001	Ventilador	7	2025-05-25
49	Samsung	Lavavajillas 12 cubiertos	SN-LV-0001	Lavavajillas	10	2026-03-10
70	Brother	DCP-T520W	SN-IM-0003	Impresora	1	2025-07-16
45	Consul	Facilite 11kg	SN-LA-0006	Lavarropas	6	2026-08-05
60	Lenovo	IdeaPad Slim 3 14	SN-NB-0002	Notebook	1	2026-07-21
75	LG	Microondas NeoChef 30L	SN-MO-0002	Microondas	6	2026-07-06
43	LG	TurboWash 13kg	SN-LA-0004	Lavarropas	4	2025-04-26
3	Tokyo	789	7894515	Televisor	4	2026-05-31
61	HP	Pavilion 15	SN-NB-0003	Notebook	2	2026-06-14
87	Tokyo	Ventilador de techo	SN-VE-0002	Ventilador	8	2025-04-18
2	Tokyo	qwes	2	Televisor	1	2026-07-07
91	Samsung	Smart Fridge	VTA-3-1-1	Heladera	18	2026-09-23
35	Samsung	WindFree 12000 BTU	SN-AA-0006	Aire acondicionado	6	2026-02-16
63	Acer	Aspire 5 A515	SN-NB-0005	Notebook	4	2026-04-01
88	Philips	Plancha a vapor 2400W	SN-PL-0001	Plancha	9	2026-09-03
\.


--
-- TOC entry 5134 (class 0 OID 18060)
-- Dependencies: 226
-- Data for Name: rol; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.rol (id_rol, nombre, descripcion) FROM stdin;
1	ADMINISTRADOR	Acceso total: elimina solicitudes, administra usuarios y roles
3	TECNICO	Servicio técnico autorizado: registra diagnóstico y cambia estados
4	VENDEDOR	Portal de ventas: vende artículos del stock y consulta sus propias ventas
2	ATENCION	Personal de NGO SAECA: registra clientes/solicitudes, asigna técnicos
\.


--
-- TOC entry 5140 (class 0 OID 18148)
-- Dependencies: 232
-- Data for Name: seguimiento; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.seguimiento (id_seguimiento, id_solicitud, id_usuario, fecha, estado, diagnostico) FROM stdin;
8	11	3	2026-09-24 05:50:48.936808	EN DIAGNÓSTICO	Motor quemado
\.


--
-- TOC entry 5150 (class 0 OID 18467)
-- Dependencies: 242
-- Data for Name: servicio_autorizado; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.servicio_autorizado (id_servicio, ciudad, estado, nombre) FROM stdin;
\.


--
-- TOC entry 5132 (class 0 OID 17973)
-- Dependencies: 224
-- Data for Name: solicitud; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.solicitud (id_solicitud, id_cliente, id_producto, fecha, descripcion, estado_actual) FROM stdin;
11	5	44	2026-09-24 05:50:00.40739	No centrifuga	EN DIAGNÓSTICO
\.


--
-- TOC entry 5136 (class 0 OID 18079)
-- Dependencies: 228
-- Data for Name: usuario; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.usuario (id_usuario, id_rol, nombre, correo, clave, estado) FROM stdin;
1	1	Administrador Prueba	admin@ngosaeca.com.py	$2b$10$6AQbd.y2DZqc9SpI.IlTgeKMZsTMrvATD0UXfl46wVDiiIgiKRCWC	ACTIVO
3	3	Tecnico Prueba	tecnico@ngosaeca.com.py	$2b$10$DYIXKUvFECLaJ5nbV9c4Fu1Gj3IVmEpqyzEfQlvGuZjW1O5OrD9oS	ACTIVO
4	4	Vendedor Prueba	vendedor@ngosaeca.com.py	$2a$10$DkGiCCqdHz4M/QKT8a8NyepZBf1ocmaqM6DRXRzt0NtKCL/X0xw7G	ACTIVO
2	2	Atencion Prueba	atencion@ngosaeca.com.py	$2b$10$VQ7b.UWEvdcGTnZ9AhnjeOKCZFsLuzKAYHrLmW5WoByP.JeSxIqKq	ACTIVO
\.


--
-- TOC entry 5144 (class 0 OID 18367)
-- Dependencies: 236
-- Data for Name: venta; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.venta (id_venta, id_vendedor, id_cliente, fecha, total, estado) FROM stdin;
3	1	18	2026-09-23 01:31:36.828519	2750000.00	COMPLETADA
\.


--
-- TOC entry 5166 (class 0 OID 0)
-- Dependencies: 233
-- Name: articulo_id_articulo_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.articulo_id_articulo_seq', 1, true);


--
-- TOC entry 5167 (class 0 OID 0)
-- Dependencies: 229
-- Name: asignacion_id_asignacion_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.asignacion_id_asignacion_seq', 5, true);


--
-- TOC entry 5168 (class 0 OID 0)
-- Dependencies: 219
-- Name: cliente_id_cliente_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.cliente_id_cliente_seq', 18, true);


--
-- TOC entry 5169 (class 0 OID 0)
-- Dependencies: 237
-- Name: detalle_venta_id_detalle_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.detalle_venta_id_detalle_seq', 3, true);


--
-- TOC entry 5170 (class 0 OID 0)
-- Dependencies: 239
-- Name: garantia_id_garantia_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.garantia_id_garantia_seq', 1, false);


--
-- TOC entry 5171 (class 0 OID 0)
-- Dependencies: 221
-- Name: producto_id_producto_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.producto_id_producto_seq', 91, true);


--
-- TOC entry 5172 (class 0 OID 0)
-- Dependencies: 225
-- Name: rol_id_rol_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.rol_id_rol_seq', 4, true);


--
-- TOC entry 5173 (class 0 OID 0)
-- Dependencies: 231
-- Name: seguimiento_id_seguimiento_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.seguimiento_id_seguimiento_seq', 8, true);


--
-- TOC entry 5174 (class 0 OID 0)
-- Dependencies: 241
-- Name: servicio_autorizado_id_servicio_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.servicio_autorizado_id_servicio_seq', 1, false);


--
-- TOC entry 5175 (class 0 OID 0)
-- Dependencies: 223
-- Name: solicitud_id_solicitud_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.solicitud_id_solicitud_seq', 11, true);


--
-- TOC entry 5176 (class 0 OID 0)
-- Dependencies: 227
-- Name: usuario_id_usuario_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.usuario_id_usuario_seq', 4, true);


--
-- TOC entry 5177 (class 0 OID 0)
-- Dependencies: 235
-- Name: venta_id_venta_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.venta_id_venta_seq', 3, true);


--
-- TOC entry 4957 (class 2606 OID 18365)
-- Name: articulo articulo_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.articulo
    ADD CONSTRAINT articulo_pkey PRIMARY KEY (id_articulo);


--
-- TOC entry 4953 (class 2606 OID 18179)
-- Name: asignacion asignacion_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asignacion
    ADD CONSTRAINT asignacion_pkey PRIMARY KEY (id_asignacion);


--
-- TOC entry 4937 (class 2606 OID 18447)
-- Name: cliente cliente_documento_unique; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cliente
    ADD CONSTRAINT cliente_documento_unique UNIQUE (documento);


--
-- TOC entry 4939 (class 2606 OID 18000)
-- Name: cliente cliente_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cliente
    ADD CONSTRAINT cliente_pkey PRIMARY KEY (id_cliente);


--
-- TOC entry 4961 (class 2606 OID 18403)
-- Name: detalle_venta detalle_venta_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_venta
    ADD CONSTRAINT detalle_venta_pkey PRIMARY KEY (id_detalle);


--
-- TOC entry 4963 (class 2606 OID 18465)
-- Name: garantia garantia_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.garantia
    ADD CONSTRAINT garantia_pkey PRIMARY KEY (id_garantia);


--
-- TOC entry 4941 (class 2606 OID 17971)
-- Name: producto producto_nro_serie_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.producto
    ADD CONSTRAINT producto_nro_serie_key UNIQUE (nro_serie);


--
-- TOC entry 4943 (class 2606 OID 18013)
-- Name: producto producto_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.producto
    ADD CONSTRAINT producto_pkey PRIMARY KEY (id_producto);


--
-- TOC entry 4947 (class 2606 OID 18240)
-- Name: rol rol_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rol
    ADD CONSTRAINT rol_pkey PRIMARY KEY (id_rol);


--
-- TOC entry 4955 (class 2606 OID 18253)
-- Name: seguimiento seguimiento_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.seguimiento
    ADD CONSTRAINT seguimiento_pkey PRIMARY KEY (id_seguimiento);


--
-- TOC entry 4965 (class 2606 OID 18473)
-- Name: servicio_autorizado servicio_autorizado_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.servicio_autorizado
    ADD CONSTRAINT servicio_autorizado_pkey PRIMARY KEY (id_servicio);


--
-- TOC entry 4945 (class 2606 OID 18027)
-- Name: solicitud solicitud_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitud
    ADD CONSTRAINT solicitud_pkey PRIMARY KEY (id_solicitud);


--
-- TOC entry 4949 (class 2606 OID 18092)
-- Name: usuario usuario_correo_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_correo_key UNIQUE (correo);


--
-- TOC entry 4951 (class 2606 OID 18311)
-- Name: usuario usuario_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_pkey PRIMARY KEY (id_usuario);


--
-- TOC entry 4959 (class 2606 OID 18379)
-- Name: venta venta_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.venta
    ADD CONSTRAINT venta_pkey PRIMARY KEY (id_venta);


--
-- TOC entry 4970 (class 2606 OID 18186)
-- Name: asignacion asignacion_id_solicitud_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asignacion
    ADD CONSTRAINT asignacion_id_solicitud_fkey FOREIGN KEY (id_solicitud) REFERENCES public.solicitud(id_solicitud);


--
-- TOC entry 4971 (class 2606 OID 18313)
-- Name: asignacion asignacion_id_usuario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asignacion
    ADD CONSTRAINT asignacion_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario);


--
-- TOC entry 4976 (class 2606 OID 18409)
-- Name: detalle_venta detalle_venta_id_articulo_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_venta
    ADD CONSTRAINT detalle_venta_id_articulo_fkey FOREIGN KEY (id_articulo) REFERENCES public.articulo(id_articulo);


--
-- TOC entry 4977 (class 2606 OID 18404)
-- Name: detalle_venta detalle_venta_id_venta_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_venta
    ADD CONSTRAINT detalle_venta_id_venta_fkey FOREIGN KEY (id_venta) REFERENCES public.venta(id_venta) ON DELETE CASCADE;


--
-- TOC entry 4978 (class 2606 OID 18474)
-- Name: garantia fk2tjrqe490v7sesi45memj9ri7; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.garantia
    ADD CONSTRAINT fk2tjrqe490v7sesi45memj9ri7 FOREIGN KEY (id_cliente) REFERENCES public.cliente(id_cliente);


--
-- TOC entry 4979 (class 2606 OID 18479)
-- Name: garantia fk3n5o0syq795od2ju4e2kcvjjv; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.garantia
    ADD CONSTRAINT fk3n5o0syq795od2ju4e2kcvjjv FOREIGN KEY (id_producto) REFERENCES public.producto(id_producto);


--
-- TOC entry 4966 (class 2606 OID 18416)
-- Name: producto producto_id_cliente_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.producto
    ADD CONSTRAINT producto_id_cliente_fkey FOREIGN KEY (id_cliente) REFERENCES public.cliente(id_cliente);


--
-- TOC entry 4972 (class 2606 OID 18262)
-- Name: seguimiento seguimiento_id_solicitud_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.seguimiento
    ADD CONSTRAINT seguimiento_id_solicitud_fkey FOREIGN KEY (id_solicitud) REFERENCES public.solicitud(id_solicitud);


--
-- TOC entry 4973 (class 2606 OID 18318)
-- Name: seguimiento seguimiento_id_usuario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.seguimiento
    ADD CONSTRAINT seguimiento_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario);


--
-- TOC entry 4967 (class 2606 OID 18036)
-- Name: solicitud solicitud_id_cliente_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitud
    ADD CONSTRAINT solicitud_id_cliente_fkey FOREIGN KEY (id_cliente) REFERENCES public.cliente(id_cliente);


--
-- TOC entry 4968 (class 2606 OID 18048)
-- Name: solicitud solicitud_id_producto_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitud
    ADD CONSTRAINT solicitud_id_producto_fkey FOREIGN KEY (id_producto) REFERENCES public.producto(id_producto);


--
-- TOC entry 4969 (class 2606 OID 18329)
-- Name: usuario usuario_id_rol_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_id_rol_fkey FOREIGN KEY (id_rol) REFERENCES public.rol(id_rol);


--
-- TOC entry 4974 (class 2606 OID 18385)
-- Name: venta venta_id_cliente_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.venta
    ADD CONSTRAINT venta_id_cliente_fkey FOREIGN KEY (id_cliente) REFERENCES public.cliente(id_cliente);


--
-- TOC entry 4975 (class 2606 OID 18380)
-- Name: venta venta_id_vendedor_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.venta
    ADD CONSTRAINT venta_id_vendedor_fkey FOREIGN KEY (id_vendedor) REFERENCES public.usuario(id_usuario);


-- Completed on 2026-09-24 05:57:00

--
-- PostgreSQL database dump complete
--

\unrestrict jl6t5O6vXlONZNr2R0jTsD03PxSVF6USwiKtSbYmbVZhwsDlO11Ko6YxinV7OwM

