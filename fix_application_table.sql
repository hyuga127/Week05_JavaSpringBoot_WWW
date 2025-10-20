-- FIX APPLICATION TABLE - AUTO INCREMENT
-- Chọn script phù hợp với database của bạn

-- ============================================
-- FOR MYSQL/MARIADB
-- ============================================

-- Check if table exists and alter it
ALTER TABLE application 
MODIFY COLUMN app_id BIGINT NOT NULL AUTO_INCREMENT;

-- Verify
SHOW CREATE TABLE application;


-- ============================================
-- FOR POSTGRESQL  
-- ============================================

-- Create sequence if not exists
CREATE SEQUENCE IF NOT EXISTS application_app_id_seq;

-- Set default value for app_id
ALTER TABLE application 
ALTER COLUMN app_id SET DEFAULT nextval('application_app_id_seq');

-- Set sequence owner
ALTER SEQUENCE application_app_id_seq OWNED BY application.app_id;

-- Reset sequence to current max value
SELECT setval('application_app_id_seq', (SELECT COALESCE(MAX(app_id), 1) FROM application));

-- Verify
\d application;


-- ============================================
-- ALTERNATIVE: DROP & RECREATE (MySQL)
-- ============================================

-- USE THIS ONLY IF YOU DON'T HAVE IMPORTANT DATA!

DROP TABLE IF EXISTS application;

CREATE TABLE application (
    app_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    candidate_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    created_date TIMESTAMP NOT NULL,
    app_status VARCHAR(20) NOT NULL,
    
    CONSTRAINT uq_candidate_job UNIQUE (candidate_id, job_id),
    CONSTRAINT fk_application_candidate 
        FOREIGN KEY (candidate_id) REFERENCES candidate(can_id),
    CONSTRAINT fk_application_job 
        FOREIGN KEY (job_id) REFERENCES job(job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_application_candidate ON application(candidate_id);
CREATE INDEX idx_application_job ON application(job_id);
CREATE INDEX idx_application_status ON application(app_status);
CREATE INDEX idx_application_created_date ON application(created_date DESC);


-- ============================================
-- ALTERNATIVE: DROP & RECREATE (PostgreSQL)
-- ============================================

-- USE THIS ONLY IF YOU DON'T HAVE IMPORTANT DATA!

DROP TABLE IF EXISTS application;

CREATE TABLE application (
    app_id BIGSERIAL PRIMARY KEY,
    candidate_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    created_date TIMESTAMP NOT NULL,
    app_status VARCHAR(20) NOT NULL,
    
    CONSTRAINT uq_candidate_job UNIQUE (candidate_id, job_id),
    CONSTRAINT fk_application_candidate 
        FOREIGN KEY (candidate_id) REFERENCES candidate(can_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_application_job 
        FOREIGN KEY (job_id) REFERENCES job(job_id)
        ON DELETE CASCADE
);

CREATE INDEX idx_application_candidate ON application(candidate_id);
CREATE INDEX idx_application_job ON application(job_id);
CREATE INDEX idx_application_status ON application(app_status);
CREATE INDEX idx_application_created_date ON application(created_date DESC);


-- ============================================
-- VERIFY THE FIX
-- ============================================

-- Test insert (MySQL/MariaDB)
INSERT INTO application (candidate_id, job_id, created_date, app_status) 
VALUES (1, 1, NOW(), 'PENDING');

SELECT * FROM application;

DELETE FROM application WHERE candidate_id = 1 AND job_id = 1;

-- Test insert (PostgreSQL)
INSERT INTO application (candidate_id, job_id, created_date, app_status) 
VALUES (1, 1, CURRENT_TIMESTAMP, 'PENDING');

SELECT * FROM application;

DELETE FROM application WHERE candidate_id = 1 AND job_id = 1;

