(require '[midje.sweet :refer :all]
         '[fs.core :as fs]
         '[milieu.config :as config])

(background (around :facts (config/with-env :test ?form)))
