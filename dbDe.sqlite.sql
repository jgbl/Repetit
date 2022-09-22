BEGIN TRANSACTION;
CREATE TABLE "SymptomeOFMedikament" ( `ID` INTEGER, `MedikamentID` INTEGER, `SymptomID` INTEGER, `Grade` INTEGER, `ParentSymptomID` INTEGER, FOREIGN KEY(`MedikamentID`) REFERENCES `Medikamente`(`ID`) ON DELETE CASCADE ON UPDATE CASCADE, FOREIGN KEY(`SymptomID`) REFERENCES `Symptome`(`ID`) ON DELETE CASCADE ON UPDATE CASCADE, FOREIGN KEY(`ParentSymptomID`) REFERENCES `Symptome`(`ID`) ON DELETE CASCADE ON UPDATE CASCADE );
CREATE TABLE "Symptome" (
	`ID`	INTEGER UNIQUE,
	`Text`	TEXT,
	`ShortText`	TEXT,
	`Comment`	TEXT,
	`KoerperTeilID`	INTEGER,
	`ParentSymptomID`	INTEGER,
	PRIMARY KEY(`ID`),
	FOREIGN KEY(`ParentSymptomID`) REFERENCES `Symptome`(`ID`) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE TABLE "Medikamente" (
	`ID`	INTEGER UNIQUE,
	`Name`	TEXT,
	`Koerper`	INTEGER,
	`Geist`	INTEGER,
	`Rechts`	INTEGER,
	`Links`	INTEGER,
	`PotenzMin`	INTEGER,
	`PotenzMax`	INTEGER,
	`Beschreibung`	TEXT,
	PRIMARY KEY(`ID`)
);
CREATE TABLE `LeitsymptomeOfMedikament` (
	`ID`	INTEGER UNIQUE,
	`MedikamentID`	INTEGER,
	`LeitsymptomID`	INTEGER,
	'Grad' INTEGER,
	PRIMARY KEY(`ID`),
	FOREIGN KEY(`MedikamentID`) REFERENCES Medikamente(ID) ON DELETE CASCADE ON
 UPDATE CASCADE,
	FOREIGN KEY(`LeitsymptomID`) REFERENCES Leitsymptome(ID) ON DELETE CASCADE ON
 UPDATE CASCADE
);
CREATE TABLE "Leitsymptome" (
	`ID`	INTEGER UNIQUE,
	`Name`	TEXT,
	`Grad`	INTEGER,
	PRIMARY KEY(`ID`)
);
CREATE TABLE "KoerperTeile" (
	`ID`	INTEGER UNIQUE,
	`Name`	TEXT,
	`ParentKoerperTeilID`	INTEGER,
	PRIMARY KEY(`ID`)
);
CREATE TABLE 'Favorites' ('ID' INTEGER, 'MedID' INTEGER, 'FolderID' INTEGER);
CREATE TABLE 'FavFolder' ('ID' INTEGER, 'Name' TEXT, 'ParentID' INTEGER);
CREATE TABLE "Fachbegriffe" ("ID" Integer PRIMARY KEY  NOT NULL  UNIQUE , "Text" VARCHAR NOT NULL  UNIQUE );
CREATE TABLE `Bedeutungen` (
	`ID`	Integer NOT NULL UNIQUE,
	`FachbegriffsID`	Integer NOT NULL,
	`Text`	Text NOT NULL,
	PRIMARY KEY(`ID`),
	FOREIGN KEY(`FachbegriffsID`) REFERENCES Fachbegriffe(ID) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE UNIQUE INDEX 'Medikamente_Idx_Medikament_ID' ON 'Medikamente' ('ID' )

;
CREATE UNIQUE INDEX 'Leitsymptome_Idx_Leitsymptom_ID' ON 'Leitsymptome' ('ID' )


;
CREATE UNIQUE INDEX 'KoerperTeile_Idx_Koerperteile_ID' ON "KoerperTeile" ('ID' )


;
CREATE UNIQUE INDEX 'Favorites_PrimaryKey' ON 'Favorites' ('ID' );
CREATE INDEX 'Favorites_FavoritesMedikamente' ON 'Favorites' ('MedID' );
CREATE INDEX 'Favorites_FavoritesFavFolder' ON 'Favorites' ('FolderID' );
CREATE UNIQUE INDEX 'FavFolder_PrimaryKey' ON 'FavFolder' ('ID' );
COMMIT;
