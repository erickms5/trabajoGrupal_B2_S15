import cats.effect.{IO, IOApp, Resource}
import cats.implicits.*
import fs2.io.file.{Files, Path}
import fs2.data.csv._
import fs2.data.csv.generic.semiauto._
import fs2.data.text.utf8.byteStreamCharLike
import doobie.*
import doobie.implicits.*
import doobie.hikari.HikariTransactor
import com.typesafe.config.ConfigFactory
import scala.concurrent.ExecutionContext

case class Estudiante(
                       nombre: String,
                       edad: Int,
                       calificacion: Int,
                       genero: String
                     )

object Database {
  private val connectEC: ExecutionContext = ExecutionContext.global

  def transactor: Resource[IO, HikariTransactor[IO]] = {
    val config = ConfigFactory.load().getConfig("db")
    HikariTransactor.newHikariTransactor[IO](
      config.getString("driver"),
      config.getString("url"),
      config.getString("user"),
      config.getString("password"),
      connectEC
    )
  }
}

object EstudianteDAO {
  def insert(estudiante: Estudiante): ConnectionIO[Int] = {
    sql"""
     INSERT INTO estudiantes (nombre, edad, calificacion, genero)
     VALUES (
       ${estudiante.nombre},
       ${estudiante.edad},
       ${estudiante.calificacion},
       ${estudiante.genero}
     )
   """.update.run
  }

  def insertAll(estudiantes: List[Estudiante]): ConnectionIO[Int] = {
    estudiantes.traverse(insert).map(_.sum)
  }
}

object Main extends IOApp.Simple {
  private val path2DataFile = "src/main/resources/data/estudiantes.csv"

  given CsvRowDecoder[Estudiante, String] = deriveCsvRowDecoder

  private def estudianteStream: fs2.Stream[IO, Estudiante] =
    Files[IO]
      .readAll(Path(path2DataFile))
      .through(decodeUsingHeaders[Estudiante](','))

  override def run: IO[Unit] = {
    Database.transactor.use { xa =>
        estudianteStream
          .compile
          .toList
          .flatMap { lista =>
            IO.println(s"Se leyó ${lista.size} estudiantes del CSV") >>
              EstudianteDAO.insertAll(lista)
                .transact(xa)
                .flatMap(total => IO.println(s"✓ Éxito: Se insertaron $total registros en la base de datos"))
          }
      }
      .handleErrorWith(e => IO.println(s"✗ Error: ${e.getMessage}"))
  }
}