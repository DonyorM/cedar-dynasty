(ns great-dalmuti.utils-test
  (:require [great-dalmuti.test :refer [defspec-test]]
            [great-dalmuti.spec :as spec]
            [great-dalmuti.utils :as u]
            [clojure.test :refer :all]))

(defspec-test spec-user-for-id `u/user-for-id)


