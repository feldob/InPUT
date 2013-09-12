CREATE TABLE input.design_space
(
	id varchar(100) NOT NULL PRIMARY KEY,
	content xml NOT NULL
);

CREATE TABLE input.design
(
	id varchar(100) NOT NULL PRIMARY KEY,
	design_space varchar(100) NOT NULL REFERENCES input.design_space(id),
	content xml NOT NULL
);

CREATE TABLE input.programming_language
(
	name varchar(100) NOT NULL PRIMARY KEY
);
			
CREATE TABLE input.framework
(
	id varchar(100) NOT NULL PRIMARY KEY
);
			
CREATE TABLE input.mappings
(
	id varchar(100) NOT NULL,
	-- an arbitrary string used for distinguishing mappings with identical ID:s, not necessarily a timestamp string
	version varchar(100) DEFAULT CAST (CURRENT_TIMESTAMP AS varchar(100)) NOT NULL,
	programming_language varchar(100) NOT NULL REFERENCES input.programming_language(name),
	framework varchar(100) REFERENCES input.framework(id),
	design_space varchar(100) NOT NULL REFERENCES input.design_space(id),
	content xml NOT NULL,
	PRIMARY KEY(id, version),
	UNIQUE(id, programming_language, framework),
	UNIQUE(programming_language, framework, design_space)
);

CREATE TABLE input.input
(
	id varchar(100) NOT NULL PRIMARY KEY,
	algorithm_design_space varchar(100) REFERENCES input.design_space(id),
	property_space varchar(100) REFERENCES input.design_space(id),
	problem_feature_space varchar(100) REFERENCES input.design_space(id),
	output_space varchar(100) REFERENCES input.design_space(id)
);
			
CREATE TABLE input.experiment
(
	id varchar(100) NOT NULL PRIMARY KEY,
	input varchar(100) NOT NULL REFERENCES input.input(id),
	algorithm_design varchar(100) REFERENCES input.design(id),
	problem_features varchar(100) REFERENCES input.design(id),
	preferences varchar(100) REFERENCES input.design(id)
);
			
CREATE TABLE input.experiment_content
(
	experiment varchar(100) NOT NULL REFERENCES input.experiment(id),
	name varchar(100) NOT NULL,
	-- 'bytea' is used instead of 'blob' in PostgreSQL databases
	content blob NOT NULL,
	PRIMARY KEY(experiment, name)
);

CREATE TABLE input.experiment_output
(
	experiment varchar(100) NOT NULL REFERENCES input.experiment(id),
	output varchar(100) NOT NULL REFERENCES input.design(id),
	PRIMARY KEY(experiment, output)
);
