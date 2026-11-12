-- 演示库表结构：字段按《金蝶采购收货单数据库 ER 图》建立
-- t_im_purrecbill（48 列）与 t_im_purrecbillentry（151 列）按需求只建精简白名单字段
-- 其余 11 张表建立 ER 图中的全部字段，保证前端能看到完整列集合
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
  fbillstatus VARCHAR(30),
  fbiztime TIMESTAMP,
  fcreatorid BIGINT,
  fcreatetime TIMESTAMP,
  fmodifierid BIGINT,
  fmodifytime TIMESTAMP,
  fauditorid BIGINT,
  fauditdate TIMESTAMP,
  fbilltypeid BIGINT,
  fsupplierid BIGINT,
  forgid BIGINT,
  facceptancestatus VARCHAR(10),
  fcomment VARCHAR(500)
);

CREATE TABLE t_im_purrecbill_l (
  fid BIGINT,
  fcomment VARCHAR(500),
  flocaleid VARCHAR(20),
  fpkid VARCHAR(64)
);

CREATE TABLE t_im_purrecbill_lk (
  fid BIGINT,
  fsbillid BIGINT,
  fstableid BIGINT,
  fsid BIGINT,
  fseq INT,
  fpkid BIGINT
);

CREATE TABLE t_im_purrecbill_tc (
  fid BIGINT,
  ftbillid BIGINT,
  fttableid BIGINT,
  fsbillid BIGINT,
  fstableid BIGINT,
  fsid BIGINT,
  ftid BIGINT
);

CREATE TABLE t_im_purrecbill_wb (
  fid BIGINT,
  foperate VARCHAR(30),
  fruleverid BIGINT,
  fsbillid BIGINT,
  fstableid BIGINT,
  fsid BIGINT,
  fwritevalue DECIMAL(18, 4),
  fseq INT,
  fentryid BIGINT,
  fruleitemid BIGINT
);

CREATE TABLE t_im_purrecbill_f (
  fid BIGINT,
  fexchangerate DECIMAL(18, 4),
  fistax VARCHAR(10),
  fpaymode VARCHAR(50),
  fexratetable BIGINT,
  finvoicebiztypeid BIGINT,
  fpayconditionid BIGINT,
  fexratedate TIMESTAMP,
  fcurrencyid BIGINT,
  fsettlecurrencyid BIGINT,
  fsettletypeid BIGINT
);

CREATE TABLE t_im_purrecbillentry (
  fentryid BIGINT PRIMARY KEY,
  fid BIGINT,
  fseq BIGINT,
  fmaterialid BIGINT,
  fmaterialname VARCHAR(200),
  fqty DECIMAL(18, 4),
  fbaseqty DECIMAL(18, 4),
  fprice DECIMAL(18, 4),
  fpriceandtax DECIMAL(18, 4),
  famount DECIMAL(18, 4),
  famountandtax DECIMAL(18, 4),
  fcuramount DECIMAL(18, 4),
  ftaxamount DECIMAL(18, 4),
  ftaxrate DECIMAL(9, 4),
  fwarehouseid BIGINT,
  flotnumber VARCHAR(64),
  fk_usp_textfield1 VARCHAR(64),
  fk_usp_deliverybill VARCHAR(64),
  fk_usp_inspect_doc_no VARCHAR(64),
  fentrycomment VARCHAR(500)
);

CREATE TABLE t_im_purrecbillentry_l (
  fprovideraddress VARCHAR(500),
  flocaleid VARCHAR(20),
  fpkid VARCHAR(64),
  fentryid BIGINT
);

CREATE TABLE t_im_purrecbillentry_r (
  fid BIGINT,
  fconcessionqty DECIMAL(18, 4),
  fsrcsystem VARCHAR(64),
  flogisticsbill VARCHAR(10),
  finvbaseqty DECIMAL(18, 4),
  fmainbillentity VARCHAR(100),
  funqualifiedunit3rd DECIMAL(18, 4),
  funverifybaseqty DECIMAL(18, 4),
  ftotalreturnqty DECIMAL(18, 4),
  fqualifiedunit2nd DECIMAL(18, 4),
  fremaininvqty DECIMAL(18, 4),
  fsrcbillentryseq BIGINT,
  fconbillrownum VARCHAR(64),
  fjoinpriceqty DECIMAL(18, 4),
  fremainpurqty DECIMAL(18, 4),
  fconbillnumber VARCHAR(64),
  fmainbillid BIGINT,
  facceptanceentrystatus VARCHAR(10),
  fsrcsysbillno VARCHAR(64),
  fsrcbillnumber VARCHAR(64),
  funqualifiedqty DECIMAL(18, 4),
  fsrcbillid BIGINT,
  finvqty DECIMAL(18, 4),
  fmainbillnumber VARCHAR(64),
  fmversion BIGINT,
  fverifybaseqty DECIMAL(18, 4),
  fsrcsysbillentryid VARCHAR(64),
  fremainjoinpricebaseqty DECIMAL(18, 4),
  fremainjoinpriceqty DECIMAL(18, 4),
  funverifyqty DECIMAL(18, 4),
  fentryid BIGINT,
  fpurbillentryid BIGINT,
  fconbillid BIGINT,
  fmainbillentryseq BIGINT,
  funqualifiedunit2nd DECIMAL(18, 4),
  fconbillentity VARCHAR(100),
  fconbillentryid BIGINT,
  fjoinpricebaseqty DECIMAL(18, 4),
  fmftorderid BIGINT,
  fconcessionbaseqty DECIMAL(18, 4),
  ftotalreturnbaseqty DECIMAL(18, 4),
  fqualifiedunit3rd DECIMAL(18, 4),
  fremainreturnbaseqty DECIMAL(18, 4),
  fpurbillnumber BIGINT,
  fverifyqty DECIMAL(18, 4),
  fqualifiedqty DECIMAL(18, 4),
  fmftorderentryseq BIGINT,
  fsrcbillentity VARCHAR(100),
  fpurchasedqty DECIMAL(18, 4),
  fqualifiedbaseqty DECIMAL(18, 4),
  fmftorderentryid BIGINT,
  funqualifiedbaseqty DECIMAL(18, 4),
  fremaininvbaseqty DECIMAL(18, 4),
  fsrcbillentryid BIGINT,
  fpurorderbillnumber VARCHAR(64),
  fmainbillentryid BIGINT,
  facceptancedate TIMESTAMP,
  fpurchasedamount DECIMAL(18, 4),
  fremainpuramount DECIMAL(18, 4),
  fremainreturnqty DECIMAL(18, 4),
  fsrcsysbillid VARCHAR(64),
  fmftordernumber VARCHAR(64)
);

CREATE TABLE t_im_purrecbillentry_lk (
  fbaseqty_old DECIMAL(18, 4),
  fk_usp_qtyfield2 DECIMAL(18, 4),
  fk_usp_baseunitqtyfield2 DECIMAL(18, 4),
  fsbillid BIGINT,
  fk_usp_baseunitqtyfield2_old DECIMAL(18, 4),
  fstableid BIGINT,
  fsid BIGINT,
  fseq INT,
  fk_usp_qtyfield2_old DECIMAL(18, 4),
  fbaseqty DECIMAL(18, 4),
  fentryid BIGINT,
  fpkid BIGINT PRIMARY KEY
);

CREATE TABLE tk_usp_receivingboxentry (
  fk_usp_sendboxno VARCHAR(64),
  fk_usp_wxno VARCHAR(64),
  fk_usp_srcsubentryid BIGINT,
  fseq INT,
  fk_usp_arrived VARCHAR(10),
  fk_usp_realqty DECIMAL(18, 4),
  fdetailid BIGINT PRIMARY KEY,
  fk_usp_detailqty DECIMAL(18, 4),
  fentryid BIGINT
);

CREATE TABLE tk_usp_cgshentryattach (
  fk_usp_is_special VARCHAR(10),
  fk_usp_fileid BIGINT,
  fk_usp_download_link VARCHAR(500),
  fk_usp_checkboxfield VARCHAR(10),
  fk_usp_isfolder VARCHAR(10),
  fseq INT,
  fdetailid BIGINT PRIMARY KEY,
  fk_usp_isupload VARCHAR(10),
  fentryid BIGINT,
  fk_usp_attachname VARCHAR(200),
  fk_usp_pre_link VARCHAR(500)
);

CREATE TABLE tk_usp_cgentryattachment (
  fbasedataid BIGINT,
  fdetailid BIGINT,
  fpkid BIGINT PRIMARY KEY
);
