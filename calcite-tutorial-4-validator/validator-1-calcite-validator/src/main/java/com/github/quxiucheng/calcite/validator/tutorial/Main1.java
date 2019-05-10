package com.github.quxiucheng.calcite.validator.tutorial;

import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.config.Lex;
import org.apache.calcite.jdbc.CalcitePrepare;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.server.CalciteServerStatement;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.dialect.OracleSqlDialect;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.type.SqlTypeFactoryImpl;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql.validate.SqlValidatorScope;
import org.apache.calcite.sql.validate.SqlValidatorUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * SqlOrderBy -> SqlSelect
 * SqlMerge -> SqlSelect
 * SqlDelete -> SqlSelect
 * SqlUpdate -> SqlSelect
 *
 * @author quxiucheng
 * @date 2019-04-26 10:18:00
 */
public class Main1 {
    public static void main(String[] args) throws SqlParseException, SQLException {

        Properties info = new Properties();
        info.put("lex", "mysql");
        String model = "calcite-tutorial-3-schema/src/main/resources/model.json";
        info.put("model", model);
        Connection connection = DriverManager.getConnection("jdbc:calcite:", info);
        CalciteServerStatement statement = connection.createStatement().unwrap(CalciteServerStatement.class);
        CalcitePrepare.Context prepareContext = statement.createPrepareContext();
        // 解析配置 - mysql设置
        SqlParser.Config mysqlConfig = SqlParser.configBuilder().setLex(Lex.MYSQL).build();
        // 创建解析器
        SqlParser parser = SqlParser.create("", mysqlConfig);
        // Sql语句
        String sql = "select * from tutorial.user_info where id = 1";
        // 解析sql
        SqlNode sqlNode = parser.parseQuery(sql);
        // 还原某个方言的SQL
        System.out.println(sqlNode.toSqlString(OracleSqlDialect.DEFAULT));
        // CalciteSchema rootSchema = CalciteSchema.createRootSchema(false, false);
        //note: 二、sql validate（会先通过Catalog读取获取相应的metadata和namespace）
//note: get metadata and namespace
        SqlTypeFactoryImpl factory = new SqlTypeFactoryImpl(RelDataTypeSystem.DEFAULT);
        CalciteCatalogReader calciteCatalogReader = new CalciteCatalogReader(
                prepareContext.getRootSchema(),
                prepareContext.getDefaultSchemaPath(),
                factory,
                new CalciteConnectionConfigImpl(new Properties()));

// //note: 校验（包括对表名，字段名，函数名，字段类型的校验。）
        SqlValidator validator = SqlValidatorUtil.newValidator(SqlStdOperatorTable.instance(),
                calciteCatalogReader, factory, SqlConformanceEnum.DEFAULT
        );
        // SqlValidatorScope scope = new SelectScope();

        // SqlNode validateSqlNode = validator.validate(sqlNode);
        SqlValidatorScope selectScope = validator.getSelectScope((SqlSelect) sqlNode);
        System.out.println(selectScope);
        // System.out.println(validateSqlNode);
    }
}
