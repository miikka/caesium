(ns caesium.crypto.generichash-test
  (:require
   [caesium.crypto.generichash :as g]
   [caesium.util :refer [unhexify array-eq]]
   [clojure.test :refer :all]
   [caesium.vectors :as v]
   [caesium.util :as u]))

(def blake2b-vector
  (comp v/hex-resource (partial str "vectors/generichash/blake2b/")))

(deftest generichash-kat-test
  (are [args expected] (and
                        (array-eq (apply g/hash args) expected)
                        (array-eq (apply g/blake2b args) expected))
    [(byte-array [])]
    (blake2b-vector "digest-empty-string")

    [(byte-array [90])]
    (blake2b-vector "digest-0")))

(deftest blake2b-kat-test
  (testing "blake2b works directly"
    (are [args expected] (array-eq (apply g/blake2b args) expected)
      [(byte-array [])]
      (blake2b-vector "digest-empty-string")

      [(byte-array [90])]
      (blake2b-vector "digest-0")

      [(.getBytes "The quick brown fox jumps over the lazy dog")
       {:key (.getBytes "This is a super secret key. Ssshh!")
        :salt (.getBytes "0123456789abcdef")
        :personal (.getBytes "fedcba9876543210")}]
      (blake2b-vector "digest-with-key-salt-personal"))))

(def blake2b-empty-args-variations
  "All of the different ways you could spell that you want the digest
  of the empty string: with or without key, salt, and
  personalization.

  When given to the blake2b function, all of these should return the
  empty string digest."
  (for [key-expr [nil {:key (byte-array 0)}]
        salt-expr [nil {:salt (byte-array 16)}]
        personal-expr [nil {:personal (byte-array 16)}]]
    [(byte-array 0) (merge key-expr salt-expr personal-expr)]))

(deftest blake2b-empty-args-variations-tests
  (doseq [args blake2b-empty-args-variations]
    (is (array-eq (apply g/blake2b args) (blake2b-vector "digest-empty-string"))
        (str "args: " args))))
