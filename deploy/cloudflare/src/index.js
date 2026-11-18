// Worker de entrada: todas las peticiones a la API se reenvian al contenedor
// del backend Spring Boot (puerto 8080).
import { Container, getContainer } from "@cloudflare/containers";

export class TalentmatchBackend extends Container {
  defaultPort = 8080;
  // Apaga el contenedor tras 30 minutos sin trafico (fuera del horario laboral, RNF-02).
  sleepAfter = "30m";

  constructor(ctx, env) {
    super(ctx, env);
    // Secretos y variables que necesita Spring Boot (ver .env.example).
    this.envVars = {
      DB_URL: env.DB_URL,
      DB_USER: env.DB_USER,
      DB_PASSWORD: env.DB_PASSWORD,
      JWT_SECRET: env.JWT_SECRET,
      CORS_ORIGENES: env.CORS_ORIGENES,
    };
  }
}

export default {
  async fetch(request, env) {
    // Una sola instancia con nombre fijo: todas las peticiones llegan al mismo backend.
    return getContainer(env.BACKEND, "principal").fetch(request);
  },
};
