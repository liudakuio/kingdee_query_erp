-- 演示库表结构：按《金蝶采购收货单数据结构与关联关系》中的 13 张表建立
-- 仅用于开箱验证，字段以文档中已确认的字段为主，并补充少量展示字段
DROP TABLE IF EXISTS t_im_purrecbill;
DROP TABLE IF EXISTS t_im_purrecbill_l;
DROP TABLE IF EXISTS t_im_purrecbill_lk;
DROP TABLE IF EXISTS t_im_purrecbill_tc;
DROP TABLE IF EXISTS t_im_purrecbill_wb;
DROP TABLE IF EXISTS t_im_purrecbill_f;
DROP TABLE IF EXISTS t_im_purrecbillentry;
DROP TABLE IF EXISTS t_im_purrecbillentry_l;
DROP TABLE IF EXISTS t_im_purrecbillentry_r;
DROP TABLE IF EXISTS t_im_purrecbillentry_lk;
DROP TABLE IF EXISTS tk_usp_receivingboxentry;
DROP TABLE IF EXISTS tk_usp_cgshentryattach;
DROP TABLE IF EXISTS tk_usp_cgentryattachment;

CREATE TABLE t_im_purrecbill (
  fid BIGINT PRIMARY KEY,
  fbillno VARCHAR(64),
  fbizdate DATE,
  fstatus VARCHAR(30),
  fsuppliername VARCHAR(200),
  fcreatorname VARCHAR(100),
  fcreatetime TIMESTAMP,
  fremark VARCHAR(500)
);

CREATE TABLE t_im_purrecbill_l (
  fid BIGINT,
  flocale VARCHAR(20),
  fname VARCHAR(200)
);

CREATE TABLE t_im_purrecbill_lk (
  fid BIGINT,
  fsourcebillno VARCHAR(64),
  fsourcebilltype VARCHAR(100)
);

CREATE TABLE t_im_purrecbill_tc (
  fid BIGINT,
  ftrackno VARCHAR(64),
  ftrackstatus VARCHAR(50)
);

CREATE TABLE t_im_purrecbill_wb (
  fid BIGINT,
  fsrcbillno VARCHAR(64),
  fwritebackqty DECIMAL(18, 4),
  fwritebacktime TIMESTAMP
);

CREATE TABLE t_im_purrecbill_f (
  fid BIGINT,
  fcustomfield1 VARCHAR(200),
  fcustomfield2 VARCHAR(200)
);

CREATE TABLE t_im_purrecbillentry (
  fentryid BIGINT PRIMARY KEY,
  fid BIGINT,
  fseq INT,
  fmaterialid VARCHAR(64),
  fmaterialname VARCHAR(200),
  fqty DECIMAL(18, 4),
  fwarehouseid VARCHAR(64),
  fk_usp_deliverybill VARCHAR(64),
  fk_usp_inspect_doc_no VARCHAR(64)
);

CREATE TABLE t_im_purrecbillentry_l (
  fentryid BIGINT,
  flocale VARCHAR(20),
  fname VARCHAR(200)
);

CREATE TABLE t_im_purrecbillentry_r (
  fentryid BIGINT,
  fbaseqty DECIMAL(18, 4),
  fprice DECIMAL(18, 4),
  famount DECIMAL(18, 4),
  ftaxrate DECIMAL(9, 4)
);

CREATE TABLE t_im_purrecbillentry_lk (
  fpkid BIGINT PRIMARY KEY,
  fentryid BIGINT,
  fsourceentryid VARCHAR(64),
  flinkqty DECIMAL(18, 4)
);

CREATE TABLE tk_usp_receivingboxentry (
  fdetailid BIGINT PRIMARY KEY,
  fentryid BIGINT,
  fboxno VARCHAR(64),
  fboxqty DECIMAL(18, 4),
  fbatchno VARCHAR(64),
  fboxstatus VARCHAR(50)
);

CREATE TABLE tk_usp_cgshentryattach (
  fdetailid BIGINT PRIMARY KEY,
  fentryid BIGINT,
  fattachname VARCHAR(200),
  fattachtype VARCHAR(50),
  frequired VARCHAR(10),
  fcheckresult VARCHAR(50)
);

CREATE TABLE tk_usp_cgentryattachment (
  fpkid BIGINT PRIMARY KEY,
  fdetailid BIGINT,
  ffilename VARCHAR(200),
  ffileurl VARCHAR(500),
  ffilesize BIGINT,
  fuploadtime TIMESTAMP
);
