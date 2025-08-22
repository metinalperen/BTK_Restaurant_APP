-- Reservations tablosunda reservation_time alanını kaldır ve yeni alanlar ekle

-- Önce yeni alanları ekle
ALTER TABLE reservations ADD COLUMN reservation_date DATE;
ALTER TABLE reservations ADD COLUMN reservation_time_new TIME;

-- Mevcut reservation_time verilerini yeni alanlara taşı
UPDATE reservations 
SET 
    reservation_date = CASE 
        WHEN reservation_time ~ '^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$' 
        THEN (reservation_time::TIMESTAMP)::DATE
        ELSE NULL 
    END,
    reservation_time_new = CASE 
        WHEN reservation_time ~ '^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$' 
        THEN (reservation_time::TIMESTAMP)::TIME
        ELSE NULL 
    END;

-- Eğer bazı kayıtlar dönüştürülemezse, bunları temizle
DELETE FROM reservations WHERE reservation_date IS NULL OR reservation_time_new IS NULL;

-- Eski reservation_time alanını kaldır
ALTER TABLE reservations DROP COLUMN reservation_time;

-- Yeni alanı doğru isimle yeniden adlandır
ALTER TABLE reservations RENAME COLUMN reservation_time_new TO reservation_time;

-- Yeni alanları NOT NULL yap
ALTER TABLE reservations ALTER COLUMN reservation_date SET NOT NULL;
ALTER TABLE reservations ALTER COLUMN reservation_time SET NOT NULL;

-- İndeksler ekle (performans için)
CREATE INDEX idx_reservations_date ON reservations(reservation_date);
CREATE INDEX idx_reservations_time ON reservations(reservation_time);
CREATE INDEX idx_reservations_date_time ON reservations(reservation_date, reservation_time);
