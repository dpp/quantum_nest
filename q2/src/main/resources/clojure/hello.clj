#_{:clj-kondo/ignore [:namespace-name-mismatch :unused-binding :unused-public-var]}
(ns yak-smell)

(require 'clojure.string)


(defn compile-functions [namespace code]
  (let [user-ns (create-ns (symbol namespace))]
    (binding [*ns* user-ns]
      (eval (read-string "(clojure.core/refer 'clojure.core)"))
      (let [mapped-list
            (map (fn [[name params snippet]]
                   (let [to-eval (str "(defn " name
                                      " [" (clojure.string/join " " params) "] "
                                      snippet ")")
                         the-symbols (read-string to-eval)
                         the-fn (eval the-symbols)]
                     [name the-fn])) code)]
        (into {} mapped-list)))))


(defn foo []
  42)