package de.tkip.sbpm.persistence.schema
import de.tkip.sbpm.persistence.mapping._
import scala.slick.lifted.ForeignKeyAction._

trait UserIdentitiesSchema extends UsersSchema {
  import driver.simple._

  object UserIdentities extends SchemaTable[UserIdentity]("user_identities") {
    def userId = column[Int]("user_id")
    def provider = column[String]("provider", DbType.stringIdentifier)
    def eMail = column[String]("e_mail", DbType.eMail)
    def password = column[Option[String]]("password", DbType.bcrypt)
    
    def * = userId ~ provider ~ eMail ~ password <> (UserIdentity, UserIdentity.unapply _)

    def pk = primaryKey(pkName, (userId, provider))

    def uniqueEmail =
      index("unq_" + tableName + "_provider_e_mail", (provider, eMail), unique = true)

    def user =
      foreignKey(fkName("users"), userId, Users)(_.id, NoAction, Cascade)
  }

}