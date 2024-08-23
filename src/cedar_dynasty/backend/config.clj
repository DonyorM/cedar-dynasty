(ns cedar-dynasty.backend.config)

(def COGNITO_CLIENT_ID (System/getenv "COGNITO_CLIENT_ID"))

(def COGNITO_CLIENT_SECRET (System/getenv "COGNITO_CLIENT_SECRET"))

(def COGNITO_UI_URL (System/getenv "COGNITO_UI_URL"))

(def COGNITO_KEYS_URL (System/getenv "COGNITO_KEYS_URL"))