import http from 'k6/http';
import { check } from 'k6';

import { FormData } from 'https://jslib.k6.io/formdata/0.0.2/index.js';
import { config } from '../config.js'
export { options } from '../options.js'

const BASE_URL = config.host;
const ENDPOINT = config.endpoint.tls;
const GOST_TLS_URL = 'https://cryptopro.ru'
const URL = `${BASE_URL}${ENDPOINT}`

export default () => {

  const fd = new FormData()
  fd.append('mTLS', 'true')
  fd.append('url', `${GOST_TLS_URL}`)

  const headers = {'Content-Type': 'multipart/form-data; boundary=' + fd.boundary}

  const res = http.post(URL, fd.body(), { headers: headers});
  check(res, {
    'tls test status is 200': (r) => r.status === 200
  });
}
