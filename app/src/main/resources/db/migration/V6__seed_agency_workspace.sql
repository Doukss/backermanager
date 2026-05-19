-- ============================================================
--  V6 — Seed agency workspace
--  Requires clean agencies-side CHECK constraints (V2 / V7)
--  Runs idempotently with ON CONFLICT DO NOTHING
-- ============================================================

INSERT INTO agencies (tenant_id, nom, adresse, telephone, email, plan, active)
VALUES
    ('dakar-prestige',
     'Dakar Prestige Immobilier',
     'Dakar Plateau',
     '+221 77 123 45 67',
     'contact@dakarprestige.sn',
     'PRO',
     true)
ON CONFLICT (tenant_id) DO UPDATE SET
    nom       = EXCLUDED.nom,
    adresse   = EXCLUDED.adresse,
    telephone = EXCLUDED.telephone,
    email     = EXCLUDED.email,
    plan      = EXCLUDED.plan,
    active    = EXCLUDED.active;

INSERT INTO subscriptions (tenant_id, plan, status, monthly_price, started_at, next_billing_at)
VALUES
    ('dakar-prestige',
     'PRO',
     'ACTIVE',
     15000,
     CURRENT_DATE - INTERVAL '2 months',
     CURRENT_DATE + INTERVAL '1 month')
ON CONFLICT (tenant_id) DO UPDATE SET
    plan            = EXCLUDED.plan,
    status          = EXCLUDED.status,
    monthly_price   = EXCLUDED.monthly_price,
    started_at      = EXCLUDED.started_at,
    next_billing_at = EXCLUDED.next_billing_at;

INSERT INTO users (tenant_id, email, password, full_name, phone, role, active)
VALUES
    ('dakar-prestige',
     'admin.agence@kermanager.local',
     '{noop}agence123',
     'Admin Agence Dakar Prestige',
     '+221 77 123 45 67',
     'ADMIN_AGENCE',
     true)
ON CONFLICT (email) DO NOTHING;

WITH inserted_properties AS (
    INSERT INTO properties (tenant_id, titre, adresse, ville, type, loyer_mensuel, surface, nombre_pieces, disponible)
    VALUES
        ('dakar-prestige', 'Appartement A12',  'Rue 10',         'Dakar Plateau', 'APPARTEMENT', 450000,  95,  4, false),
        ('dakar-prestige', 'Studio B4',        'Corniche Ouest',  'Almadies',      'APPARTEMENT', 300000,  45,  1, false),
        ('dakar-prestige', 'Villa C7',         'Mermoz',          'Dakar',         'MAISON',      850000, 220,  6, true)
    ON CONFLICT DO NOTHING
    RETURNING id, titre, loyer_mensuel
),
inserted_contracts AS (
    INSERT INTO contracts (tenant_id, property_id, locataire_nom, locataire_email, date_debut, date_fin, loyer_mensuel, depot, statut)
    SELECT
        'dakar-prestige'  AS tenant_id,
        p.id              AS property_id,
        CASE p.titre
            WHEN 'Appartement A12' THEN 'Mamadou Fall'
            WHEN 'Studio B4'       THEN 'Mariama Sow'
            ELSE                      'Cheikh Ba'
        END               AS locataire_nom,
        CASE p.titre
            WHEN 'Appartement A12' THEN 'mamadou.fall@email.sn'
            WHEN 'Studio B4'       THEN 'mariama.sow@email.sn'
            ELSE                      'cheikh.ba@email.sn'
        END               AS locataire_email,
        CURRENT_DATE - INTERVAL '3 months' AS date_debut,
        CURRENT_DATE + INTERVAL '9 months' AS date_fin,
        p.loyer_mensuel,
        p.loyer_mensuel,
        CASE p.titre WHEN 'Villa C7' THEN 'BROUILLON' ELSE 'ACTIF' END AS statut
    FROM inserted_properties p
    ON CONFLICT DO NOTHING
    RETURNING id, locataire_nom, loyer_mensuel
)
INSERT INTO payments (tenant_id, contract_id, montant, date_echeance, date_paiement, statut, reference)
SELECT
    'dakar-prestige',
    c.id,
    c.loyer_mensuel,
    CURRENT_DATE - INTERVAL '10 days',
    CASE WHEN c.locataire_nom = 'Mamadou Fall' THEN NULL ELSE CURRENT_DATE - INTERVAL '7 days' END,
    CASE WHEN c.locataire_nom = 'Mamadou Fall' THEN 'EN_RETARD' ELSE 'PAYE' END,
    'LOY-' || upper(left(replace(c.locataire_nom, ' ', ''), 6))
FROM inserted_contracts c
ON CONFLICT DO NOTHING;

INSERT INTO disputes (tenant_id, titre, description, statut, priorite)
VALUES
    ('dakar-prestige',
     'Retard de paiement loyer',
     'Le locataire accuse un retard de paiement sur le mois courant.',
     'EN_COURS',
     'HAUTE'),
    ('dakar-prestige',
     'Degat plomberie',
     'Fuite signalee dans la salle de bain.',
     'OUVERT',
     'NORMALE')
ON CONFLICT DO NOTHING;
