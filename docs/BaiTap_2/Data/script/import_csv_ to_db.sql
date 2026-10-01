DROP TABLE IF EXISTS tbl_temp;

-- ====Create tbl_temp======= 
CREATE TABLE tbl_temp ( 
	id             	int(11) AUTO_INCREMENT NOT NULL,
	code           	varchar(11) NULL,
	old_post_code  	varchar(11) NULL,
	post_code      	varchar(11) NULL,
	prefecture_kana	varchar(100) NULL,
	city_kana      	varchar(100) NULL,
	area_kana      	varchar(100) NULL,
	prefecture     	varchar(100) NULL,
	city           	varchar(100) NULL,
	area           	varchar(100) NULL,
	multi_post_area	int(11) NULL,
	koaza_area     	int(11) NULL,
	chome_area     	int(11) NULL,
	multi_area     	int(11) NULL,
	update_show    	int(11) NULL,
	change_reason  	int(11) NULL,
	PRIMARY KEY(id)
)
ENGINE = InnoDB;
CREATE INDEX city_kana_index ON tbl_temp(city_kana);
CREATE INDEX old_post_index ON tbl_temp(old_post_code);
CREATE INDEX prefecture_kana_index ON tbl_temp(prefecture_kana);
CREATE INDEX post_index ON tbl_temp(post_code);
CREATE INDEX area_kana_index ON tbl_temp(area_kana);
CREATE INDEX area_index ON tbl_temp(area);
-- ===Load data=====
LOAD DATA LOCAL INFILE '<path>/KEN_ALL.CSV' INTO TABLE tbl_temp FIELDS TERMINATED BY ',' LINES TERMINATED BY '\n' 
(code,old_post_code,post_code,prefecture_kana,city_kana,area_kana,prefecture,city,area,multi_post_area,koaza_area,chome_area,multi_area,update_show,change_reason);

-- ===tbl_prefecture============================	
	INSERT INTO tbl_prefecture(prefecture_CODE,prefecture_kana,prefecture) 
	SELECT CODE, prefecture_kana,prefecture
	FROM (
	SELECT DISTINCT LEFT(CODE,2) AS CODE,prefecture_kana,prefecture
	FROM tbl_temp
	) AS T2

	ORDER BY CODE;
-- ===tbl_city============================

	INSERT INTO tbl_city(CODE,city_kana,city,prefecture_id) 
	SELECT CODE, city_kana AS city_kana,city AS city,(SELECT prefecture_id FROM tbl_prefecture WHERE prefecture_code =LEFT(CODE,2)) AS prefecture_id
	FROM (
	SELECT DISTINCT CODE,city_kana,city
	FROM tbl_temp
	) AS T2
	ORDER BY CODE;
-- ===tbl_post====================
	INSERT INTO tbl_post(post_code
		,update_show,change_reason,multi_area)
	SELECT DISTINCT post_code, update_show,change_reason,multi_area 
	FROM tbl_temp
	ORDER BY post_code;

-- ===tbl_old_post====================
	INSERT INTO tbl_old_post(old_post_code)
	SELECT DISTINCT old_post_code
	FROM tbl_temp
	ORDER BY old_post_code;
	
	
-- ====tbl_area====================

INSERT INTO tbl_area(area_kana, AREA,multi_post_area,koaza_area,chome_area,post_id,old_post_id,city_id)
	 SELECT area_kana, AREA,multi_post_area,koaza_area,chome_area,post_id, old_post_id, city_id
	 FROM
		( 
		  SELECT DISTINCT tbl_temp.area_kana, tbl_temp.area, tbl_temp.multi_post_area,tbl_temp.koaza_area,tbl_temp.chome_area,tbl_temp.code,post_id, old_post_id, 
			tbl_temp.city_kana, tbl_temp.city, tbl_temp.prefecture, tbl_temp.prefecture_kana,tbl_city.city_id
		  FROM tbl_temp
		  INNER JOIN tbl_post ON tbl_post.post_code = tbl_temp.post_code
		  INNER JOIN tbl_old_post ON tbl_old_post.old_post_code = tbl_temp.old_post_code
		  INNER JOIN tbl_prefecture ON tbl_temp.prefecture = tbl_prefecture.prefecture AND tbl_temp.prefecture_kana = tbl_prefecture.prefecture_kana
		  INNER JOIN tbl_city ON tbl_city.city_kana = tbl_temp.city_kana AND tbl_city.city = tbl_temp.city AND tbl_prefecture.prefecture_id = tbl_city.prefecture_id
		) AS tbl;

-- ======= update recode special : 0330071,0210102,9660501,9692701,9790622, (9218046) ,4901323, 7770301, 7712107, 7712106, 7712102, 7712105, 7712103, 7712104======= 

Update tbl_area join tbl_post on tbl_area.post_id = tbl_post.post_id
set 
tbl_area.area_kana = 'ｲﾇｵﾄｾ(ｳﾁｶﾅﾔ､ｳﾁﾔﾏ､ｵｶﾇﾏ､ｶﾅｻﾞﾜ､ｶﾅﾔ､ｶﾐｻﾋﾞｼﾛ､ｷｺｼ､ｺﾞﾝｹﾞﾝｻﾜ､ｼｷ､ｼﾁﾋｬｸ､ｼﾓｸﾎﾞ<174ｦﾉｿﾞｸ>､ｼﾓｻﾋﾞｼﾛ､ﾀｶﾓﾘ､ﾂﾞﾒｷ､ﾂﾎﾞｹｻﾞﾜ<25､637､641､643､647ｦﾉｿﾞｸ>､ﾅｶﾔｼｷ､ﾇﾏｸﾎﾞ､ﾈｺﾊｼ､ﾎﾘｷﾘｻﾜ､ﾐﾅﾐﾀｲ､ﾔﾅｷﾞｻﾜ､ｵｵﾏｶﾞﾘ)',
tbl_area.area = '犬落瀬（内金矢、内山、岡沼、金沢、金矢、上淋代、木越、権現沢、四木、七百、下久保「１７４を除く」、下淋代、高森、通目木、坪毛沢「２５、６３７、６４１、６４３、６４７を除く」、中屋敷、沼久保、根古橋、堀切沢、南平、柳沢、大曲）'
where tbl_post.post_code = '0330071'; 

Update tbl_area join tbl_post on tbl_area.post_id = tbl_post.post_id
set 
tbl_area.area_kana = 'ﾊｷﾞｼｮｳ(ｱｶｲﾉｺ､ｱｼﾉｸﾁ､ｱﾏﾜﾗﾋﾞ､ｵｲﾅｶﾞﾚ､ｵｵｻﾜ､ｶﾐｳﾂﾉ､ｶﾐﾎﾝｺﾞｳ､ｶﾐﾖｳｶﾞｲ､ｹｼｮｳｻﾞｶ､ｻﾝｶﾞﾂﾀﾞ､ｼﾓｳﾂﾉ､ｼﾓﾎﾝｺﾞｳ､ｿﾃﾞﾔﾏ､ﾄﾞｳﾉｻﾜ､ﾄﾁｸﾗ､ﾄﾁｸﾗﾐﾅﾐ､ﾅｶﾞｸﾗ､ﾅｶｻﾞﾜ､ﾊﾁﾓﾘ､ﾊﾞﾊﾞ､ﾋﾛｵﾓﾃ､ﾋﾗﾊﾞ､ﾌﾙｶﾏﾊﾞ､ﾏｶﾞﾘﾌﾁ､ﾏﾂﾊﾞﾗ､ﾐﾅﾐｻﾞﾜ､ﾔｷﾞ､ﾔｯｷﾘ､ﾔｾ､ﾔﾊﾀ､ﾔﾏﾉｻﾜ)',
tbl_area.area = '萩荘（赤猪子、芦ノ口、甘蕨、老流、大沢、上宇津野、上本郷、上要害、化粧坂、三月田、下宇津野、下本郷、外山、堂の沢、栃倉、栃倉南、長倉、中沢、八森、馬場、広面、平場、古釜場、曲淵、松原、南沢、谷起、焼切、八瀬、八幡、山ノ沢）'
where tbl_post.post_code = '0210102'; 

Update tbl_area join tbl_post on tbl_area.post_id = tbl_post.post_id
set 
tbl_area.area_kana = 'ﾋﾊﾞﾗ(ｳﾊﾞﾀｻﾜ､ｶﾅﾔﾏ､ｺﾞｼﾞｭｳﾘｮｳﾊﾗ､ｽﾉﾔﾏ､ﾀｷﾉﾊﾗ､ﾄﾞｳｾﾞﾝﾊﾗ､ﾊｶｼﾀ､ﾋﾊﾞﾗ､ﾑｴﾝﾊﾗ､ﾜｾｻﾞﾜ､ｺﾔｻﾜ､ﾐｽﾞﾅｼﾊﾗ､ﾅｶﾊﾗ､ﾅﾗﾉｷﾀﾞｲﾗﾊﾗ､ﾔｹｶﾞﾂﾗﾔﾏ､ﾀﾃﾔﾏ)',
tbl_area.area = '檜原（苧畑沢、金山、五十両原、巣ノ山、滝ノ原、道前原、墓下、桧原、無縁原、早稲沢、小屋沢、水梨原、中原、楢木平原、焼桂山、館山）'
where tbl_post.post_code = '9660501'; 

Update tbl_area join tbl_post on tbl_area.post_id = tbl_post.post_id
set 
tbl_area.area_kana = 'ﾋﾊﾞﾗ(ｱｷﾓﾄ､ｱﾗｽﾅｻﾜﾔﾏ､ｳﾗﾊﾞﾝﾀﾞｲ､ｵｵﾌﾞﾀﾞｲﾗ､ｵﾉｶﾞﾜ､ｵﾉｶﾞﾜﾊﾗ､ｹﾝｶﾞﾐﾈ､ｺﾀｶﾓﾘ､ｺﾞｼｷﾇﾏ､ｼﾞﾝｸﾛｳﾔﾏ､ｿﾊﾗﾔﾏ､ﾃﾗｻﾜﾔﾏ､ﾍﾋﾞﾀｲﾗﾊﾗﾔﾏ､ﾕﾀﾞｲﾗﾔﾏ､ﾔﾅﾍﾞｻﾜﾔﾏ)',
tbl_area.area = '檜原（秋元、荒砂沢山、裏磐梯、大府平、小野川、小野川原、剣ケ峯、狐鷹森、五色沼、甚九郎山、曽原山、寺沢山、蛇平原山、湯平山、簗部沢山）'
where tbl_post.post_code = '9692701'; 

Update tbl_area join tbl_post on tbl_area.post_id = tbl_post.post_id
set 
tbl_area.area_kana = 'ｹｶﾞﾔ(ﾏｴｶﾜﾊﾗ232-244､311､312､337-862ﾊﾞﾝﾁﾄｳｷｮｳﾃﾞﾝﾘｮｸﾌｸｼﾏﾀﾞｲ2ｹﾞﾝｼﾘｮｸﾊﾂﾃﾞﾝｼｮｺｳﾅｲ)',
tbl_area.area = '毛萱（前川原２３２～２４４、３１１、３１２、３３７～８６２番地〔東京電力福島第二原子力発電所構内〕）'
where tbl_post.post_code = '9790622'; 

Update tbl_area join tbl_post on tbl_area.post_id = tbl_post.post_id
set 
tbl_area.area_kana = 'ｵｵｸﾜﾏﾁ(ｱ､ｲ､ｲ､ｳ､ｳｴﾉ､ｵ､ｵｵﾂ､ｶﾈﾂｷﾔﾏ､ｶﾐｶﾜﾗ､ｶﾐﾈｺｼﾀ､ｸ､ｹ､ｺﾞｼｮｶﾞﾀﾞﾆ､ｺﾃﾞﾗﾔﾏ､ｼ､ｼﾓｳｴﾉ､ｼﾓﾆｼｶﾞｹ､ﾀﾞｲﾗ､ﾁ､ﾂｵﾂ､ﾂﾍｲ､ﾃ､ﾄ､ﾅｶｳｴﾉ､ﾅｶｵﾔﾏ､ﾅｶﾀﾞｲﾗ､ﾅｶﾉｵｵﾋﾗ､ﾆｼﾉﾔﾏ､ﾈｺﾉｼﾀｲ､ﾉ､ﾊ､ﾋﾗｷ､ﾎｳｼﾔﾏ､ﾎﾞｳﾔﾏ､ﾏ､ﾏｽｶﾜﾌﾞﾁ､ﾑ､ﾓﾄｽｴ､ﾓﾄﾜｸﾅﾐｺｳ､ﾔ､ﾘ､ﾙ､ﾚｵﾂ､ﾚｺｳ､ﾛｵﾂ､ﾛｺｳ､ﾜ)',
tbl_area.area = '大桑町（ア、イ、ヰ、ウ、上野、ヲ、オ乙、鐘搗山、上川原、上猫下、ク、ケ、御所谷、小寺山、シ、下上野、下西欠、平、チ、ツ乙、ツ丙、テ、ト、中上野、中尾山、中平、中ノ大平、西ノ山、猫シタイ、ノ、ハ、開、法師山、坊山、マ、鱒川淵、ム、元末、元涌波庚、ヤ、リ、ル、レ乙、レ甲、ロ乙、ロ甲、和）'
where tbl_post.post_code = '9218046'  and length(tbl_area.area_kana) > 100; 

Update tbl_area join tbl_post on tbl_area.post_id = tbl_post.post_id
set 
tbl_area.area_kana = 'ﾍｲﾜﾁｮｳ(ｺｳﾜ､ｼｵｶﾜ､ｼﾓｺｼｷﾀ､ｼﾓｺｼﾅｶ､ｼﾓｺｼﾋｶﾞｼ､ｼﾓｺｼﾐﾅﾐ､ｼｮﾊﾞﾀｼﾝﾃﾞﾝ､ｼﾛﾆｼ､ｼﾛﾉｳﾁ､ｽｶﾜｷ､ﾅｺﾗ､ﾍｲﾛｸ､ﾏｴﾋﾗ､ﾒｲﾜ､ﾖﾒﾌﾘ､ﾖﾒﾌﾘｷﾀ､ﾖﾒﾌﾘﾋｶﾞｼ､ﾘｮｳﾅｲ)',
tbl_area.area = '平和町（光和、塩川、下起北、下起中、下起東、下起南、勝幡新田、城西、城之内、須ケ脇、那古良、平六、前平、明和、嫁振、嫁振北、嫁振東、領内）'
where tbl_post.post_code = '4901323'; 

Update tbl_area join tbl_post on tbl_area.post_id = tbl_post.post_id
set 
tbl_area.area_kana = 'ｺﾔﾀﾞｲﾗ(ｲﾁﾊﾅ､ｲﾏﾏﾙ､ｵﾔﾏ､ｶｺﾞﾐ､ｶｼﾜﾗ､ｷﾅｶ､ｸﾜｶﾞﾗ､ｹﾔｷﾋﾗ､ｺﾋﾞｳﾗ､ｼﾞｼﾞﾝﾀﾞｷ､ｽｹﾞｿﾞｳ､ﾂｴﾀﾞﾆ､ﾂﾂﾞﾛｳ､ﾊｼﾞｺﾉ､ﾋﾞﾔｶﾞｲﾁ､ﾌﾀﾄﾞ､ﾐﾂｷﾞ､ﾐﾂｸﾞ､ﾐﾅﾐﾊﾞﾘ､ﾑｺｳｶｼﾜﾗ)',
tbl_area.area = '木屋平（市初、今丸、尾山、カゴミ、樫原、木中、桑柄、ケヤキヒラ、小日浦、地神滝、菅蔵、杖谷、葛尾、ハジコノ、ビヤガイチ、二戸、三ツ木、貢、南張、向樫原）'
where tbl_post.post_code = '7770301'; 


Update tbl_area join tbl_post on tbl_area.post_id = tbl_post.post_id
set 
tbl_area.area_kana = 'ﾐﾏﾁｮｳ(ｱﾏｹﾞ､ｳｴﾉ､ｺﾊﾞﾗ､ｻｶｲﾒ､ｻﾄﾆｼﾔｼｷ､ｼﾓﾉﾀﾞﾝ､ｼﾛ､ﾀｷｼﾀ､ﾀｹﾉｳﾁ､ﾀﾅﾍﾞ､ﾀﾆｸﾞﾁ､ﾂｶﾞｵ､ﾂｷｵﾄｼ､ﾅｶｵ､ﾅｶｺﾞｳﾁ､ﾅｶｽｼﾞ､ﾅｶﾄﾞｵﾘ､ﾅｶﾆｼ､ﾅｶﾔﾏ､ﾅｶﾞﾊﾀ､ﾅﾂﾜﾗﾋﾞ､ﾆｼﾐﾔﾉｳｴ､ﾋｶﾞｼﾐﾔﾉｳｴ､ﾎｿﾉ､ﾏｴﾀﾞ､ﾏﾂﾉﾊﾅ､ﾏﾂﾉﾓﾄ､ﾐﾔﾏｴ､ﾐﾔｷﾀ､ﾔﾊﾀ､ﾔﾏﾅﾂﾜﾗﾋﾞ､ﾖｼﾐｽﾞ)',
tbl_area.area = '美馬町（雨下、上野、小原、境目、里西屋敷、下ノ段、城、滝下、竹ノ内、田辺、谷口、栂尾、突落、中尾、中耕地、中筋、中通、中西、中山、長畑、夏蕨、西宮ノ上、東宮ノ上、細野、前田、松ノ花、松ノ本、宮前、宮北、八幡、山夏蕨、吉水）'
where tbl_post.post_code = '7712107'; 

Update tbl_area join tbl_post on tbl_area.post_id = tbl_post.post_id
set 
tbl_area.area_kana = 'ﾐﾏﾁｮｳ(ｱﾗｶﾜ､ｲﾃﾞﾍﾞﾘ､ｲﾉｶﾐ､ｵｵｲｽﾞﾐ､ｵｵﾐﾔﾆｼ､ｶﾐﾂｷﾀﾞｼ､ｷｼﾉｼﾀ､ｷﾀﾋｶﾞｼﾊﾞﾗ､ｷﾗｲｲﾁ､ｻﾄﾋﾗﾉ､ｼﾓﾂｷﾀﾞｼ､ﾀﾆﾖﾘﾆｼ､ﾁﾁﾉｷ､ﾃﾗﾉｼﾀ､ﾃﾝｼﾞﾝ､ﾃﾝｼﾞﾝｷﾀ､ﾅｶﾋｶﾞｼﾊﾞﾗ､ﾆｼｱﾗｶﾜ､ﾇﾏﾀﾞｼﾞﾏ､ﾋｶﾞｼｱﾗｶﾜ､ﾐﾅﾐｱﾗｶﾜ､ﾐﾅﾐﾋｶﾞｼﾊﾞﾗ､ﾐｮｳｹﾝ､ﾔﾏﾖﾒｻﾞｶ)',
tbl_area.area = '美馬町（荒川、井出縁、井ノ神、大泉、大宮西、上突出、岸ノ下、北東原、喜来市、里平野、下突出、谷ヨリ西、乳ノ木、寺ノ下、天神、天神北、中東原、西荒川、沼田島、東荒川、南荒川、南東原、妙見、山嫁坂）'
where tbl_post.post_code = '7712106'; 


Update tbl_area join tbl_post on tbl_area.post_id = tbl_post.post_id
set 
tbl_area.area_kana = 'ﾐﾏﾁｮｳ(ｲｴﾉﾏｴ､ｵｵｲｹ､ｵｵｳｴ､ｵｵｷﾀ､ｵｵｻｺ､ｵｵﾏｴ､ｵｶﾉｳﾁ､ｵｼｱｹﾞ､ｶﾔﾊﾞﾗ､ｶﾜﾉｳｴ､ｶﾝﾉﾝ､ｸﾘﾊﾞﾔｼ､ｸﾛｽﾅ､ｻﾙｶﾞｳﾁ､ｻﾙｻｶ､ｼﾓｼﾛｼﾞ､ｼﾛｼﾞ､ｽﾐｶﾞﾏ､ｾﾘｻﾞｺ､ｿｳｺﾞ､ｿｳﾀﾞ､ｿﾄﾊﾞｶ､ﾀｷﾉｳｴ､ﾀﾂﾐﾔﾏ､ﾀﾉｵｶ､ﾀﾝｻﾞｶ､ﾅｶｵｶ､ﾅｶﾉﾀﾉｲ､ﾅｶﾖｺｵ､ﾅｶﾞｼﾞ､ﾆｼｳﾗ､ﾆｼﾉﾀﾆ､ﾉﾀﾉｲ､ﾊｼﾀﾞﾆ､ﾊﾞｼﾄｺ､ﾋｶﾞｼｽｼﾞ､ﾋﾗｵ､ﾋﾗﾉ､ﾌｼﾞﾕｳ､ﾏｴｻﾞｶ､ﾐｽﾞﾉｵｶ､ﾐｿｶﾞｸﾎﾞ､ﾐﾁﾉｳｴ､ﾐﾔﾉｵｶ､ﾑﾅﾊﾞﾀ､ﾔｸｼｶﾞｸﾎﾞ､ﾔﾏﾆｼﾔｼｷ､ﾔﾏﾊｼﾀﾞﾆ､ﾖｺｵ)',
tbl_area.area = '美馬町（家ノ前、大池、大上、大北、大佐古、大前、岡ノ内、押上、茅原、川ノ上、観音、栗林、黒砂、狙ケ内、猿坂、下白地、白地、炭釜、芹佐古、惣後、惣田、外墓、滝ノ上、立見山、田ノ岡、狙坂、中岡、中野田ノ井、中横尾、長地、西浦、西ノ谷、野田ノ井、橋谷、場シ所、東筋、平尾、平野、藤宇、前坂、水ノ岡、味噌ケ久保、道ノ上、宮ノ岡、南畠、薬師ケ久保、山西屋敷、山橋谷、横尾）'
where tbl_post.post_code = '7712102'; 

Update tbl_area join tbl_post on tbl_area.post_id = tbl_post.post_id
set 
tbl_area.area_kana = 'ﾐﾏﾁｮｳ(ｲｹﾉｳﾗ､ｲﾁｮｳｷﾞ､ｳﾒｶﾞｸﾎﾞ､ｴｷ､ｴﾋﾞｽ､ｶｷﾉｷ､ｶﾝｶｹ､ｶﾞﾝｼｮｳｼﾞ､ｷﾀﾂﾁｶﾞｸﾎﾞ､ｷﾆｭｳﾄﾞｳ､ﾀｷﾉﾐﾔ､ﾂﾁｶﾞｸﾎﾞ､ﾆｼﾀﾞﾝ､ﾐﾔﾋｶﾞｼ､ﾐﾔﾆｼ､ﾐﾔﾐﾅﾐ､ﾖｳｾﾝ)',
tbl_area.area = '美馬町（池ノ浦、銀杏木、梅ケ久保、駅、蛭子、柿木、鍵掛、願勝寺、北土ケ久保、喜入道、滝宮、土ケ久保、西段、宮東、宮西、宮南、養泉）'
where tbl_post.post_code = '7712105'; 

Update tbl_area join tbl_post on tbl_area.post_id = tbl_post.post_id
set 
tbl_area.area_kana = 'ﾐﾏﾁｮｳ(ｲﾁﾉﾐﾔ､ｳｼﾛﾀﾞﾆ､ｸﾗｵ､ｻﾝﾄｳｻﾞﾝ､ｼﾓｳｼﾛﾀﾞﾆ､ﾀｶｿｳ､ﾀﾆｼﾞﾘ､ﾂﾕｸﾞﾁ､ﾅｶﾇﾏﾀﾞ､ﾅｶﾉ､ﾅﾂﾔｷ､ﾅﾛｵ､ﾆｼｵｵｸﾎﾞ､ﾆｼﾇﾏﾀﾞ､ﾇﾏﾀﾞ､ﾊﾁﾉﾂﾎﾞ､ﾎﾞｳｶﾞﾀﾆ､ﾐｽﾞｸﾎﾞ､ﾑﾈﾉﾌﾞﾝ､ﾔﾅｲ)',
tbl_area.area = '美馬町（一ノ宮、後谷、倉尾、三頭山、下後谷、高惣、谷尻、露口、中沼田、中野、夏弥喜、ナロヲ、西大久保、西沼田、沼田、八ノ坪、坊ケ谷、水久保、宗ノ分、屋内）'
where tbl_post.post_code = '7712103'; 

Update tbl_area join tbl_post on tbl_area.post_id = tbl_post.post_id
set 
tbl_area.area_kana = '7712104',
tbl_area.area = 'ﾐﾏﾁｮｳ(ｳｶﾞｲｸﾞﾁ､ｵｶ､ｵﾊﾞｾ､ｵﾊﾞｾﾊﾞﾀ､ｶｻﾎﾞﾄｹ､ｶﾜﾍﾞﾘ､ｺｳｼﾞﾝ､ｽｹﾏﾂ､ﾀｶﾊﾞﾀｹ､ﾀﾂｶｸ､ﾀﾆｶﾞｼﾗ､ﾀﾏﾌﾘﾏｴ､ﾁｹﾞｼﾞ､ﾃﾝｼﾞﾝﾋｶﾞｼ､ﾄﾄﾞﾛｷ､ﾅｶｸﾞﾛ､ﾅｶｽ､ﾅｶﾐﾁｷﾀ､ﾅｶﾐﾁﾐﾅﾐ､ﾉﾂｺﾞ､ﾉﾘｺﾍ､ﾊｼﾉﾓﾄ､ﾋｶﾞｼｷｼﾉｼﾀ､ﾋｶﾞｼﾀﾞﾝ､ﾋｶﾞｼﾑﾈｼｹﾞ､ﾎﾞｳｿｳ､ﾏﾙﾔﾏ､ﾐﾅﾐﾊﾗ､ﾐｮｳｼﾞﾝﾊﾞﾗ､ﾑﾈｼｹﾞ､ﾔｸｼ)'
where tbl_post.post_code = '美馬町（鵜飼口、岡、小長谷、小長谷端、笠仏、川縁、荒神、助松、高畑、辰角、谷頭、玉振前、チゲジ、天神東、轟、中黒、中須、中道北、中道南、ノツゴ、ノリコヘ、橋本、東岸ノ下、東段、東宗重、坊僧、丸山、南原、明神原、宗重、薬師）'; 


-- ====Drop tbl_temp=======  
DROP TABLE tbl_temp; 