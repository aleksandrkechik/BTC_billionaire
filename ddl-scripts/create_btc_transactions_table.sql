
CREATE TABLE IF NOT EXISTS public.btc_transactions (
    datetime TIMESTAMP NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    PRIMARY KEY (datetime));
