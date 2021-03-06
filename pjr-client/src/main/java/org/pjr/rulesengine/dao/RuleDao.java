package org.pjr.rulesengine.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pjr.rulesengine.CommonConstants;
import org.pjr.rulesengine.DataLayerException;
import org.pjr.rulesengine.dbmodel.Operator;
import org.pjr.rulesengine.dbmodel.Rule;
import org.pjr.rulesengine.dbmodel.RuleLogic;
import org.pjr.rulesengine.dbmodel.Subrule;
import org.pjr.rulesengine.propertyloader.PropertyLoader;

/**
 * The Class RuleDao.
 *
 * @author Sudhakar
 */
public class RuleDao {
	
	/** The Constant log. */
	private static final Log log = LogFactory.getLog(RuleDao.class);

	
	/** The data source. */
	private DataSource dataSource;
	
	/**
	 * Instantiates a new rule dao.
	 *
	 * @param dataSource the data source
	 * @author  Sudhakar
	 */
	public RuleDao(DataSource dataSource){
		this.dataSource = dataSource;
	}
	
	/**
	 * Fetch rule.
	 *
	 * @param ruleId the rule id
	 * @return the rule
	 * @throws DataLayerException the data layer exception
	 * @author  Sudhakar
	 */
	public Rule fetchRule(final String ruleId) throws DataLayerException{
		
		String sql=PropertyLoader.getProperty(CommonConstants.QUERY_FETCHRULE_SELECTRULE);
		
		Rule rule;
		try {
			QueryRunner qr = new QueryRunner(dataSource);		
			Object params [] = new Object[]{ruleId};
			
			rule = qr.query(sql,  new ResultSetHandler<Rule>(){

				@Override
				public Rule handle(ResultSet rs) throws SQLException {
					Rule r = null;

					while(rs.next()){
						r  = new Rule();
						r.setId(rs.getString("RULE_ID"));
						r.setRuleName(rs.getString("RULE_NAME"));
						r.setRuleDescription(rs.getString("RULE_DESCRIPTION"));
						r.setActive(rs.getBoolean("ACTIVE"));
						r.setReturnValue(rs.getString("RETURN_VALUE"));
						r.setExecutionOrder(rs.getInt("EXE_ORDER"));
						r.setModelId(rs.getString("MODEL_ID"));
					}
					return r;
				
				}}, params);
			
			
			final SubRuleDao subruleDao = new SubRuleDao(dataSource);
			if (null != rule) {
				String ruleLogicSql=PropertyLoader.getProperty(CommonConstants.QUERY_FETCHRULE_SELECTRULELOGIC);
				QueryRunner logicQr = new QueryRunner(dataSource);	
				List<RuleLogic> logic = logicQr.query(ruleLogicSql, new ResultSetHandler<List<RuleLogic> >(){

					@Override
					public List<RuleLogic> handle(ResultSet rs) throws SQLException {
						List<RuleLogic> ruleLogicList = new ArrayList<RuleLogic>();

						while (rs.next()) {
							RuleLogic ruleLogic = new RuleLogic();

							//Rule Logic
							String ruleLogicId = rs.getString(1);
							String subruleMapId = rs.getString(2);
							String operatorId = rs.getString(3);
							int orderNo = rs.getInt(4);

							//Subrule
							String subRuleName = rs.getString(5);
							String subRuleDesription = rs.getString(6);
							boolean subRuleDefaultvalue = rs.getBoolean(7);

							Subrule sr = null;
							if (null != subruleMapId) {
								sr = new Subrule();
								sr.setId(subruleMapId);
								sr.setDescription(subRuleDesription);
								sr.setName(subRuleName);
								sr.setDefaultValue(subRuleDefaultvalue);
							}
							String subRuleId = rs.getString(10);
							Subrule subRuleTemp;
							try {

								if (null != subRuleId) {
																	
									subRuleTemp = subruleDao.fetchSubrule(subRuleId);
									sr.setLogic(subRuleTemp.getLogic());
									sr.setActive(subRuleTemp.isActive());
									sr.setDefaultValue(subRuleTemp.isDefaultValue());
								}
							} catch (Exception e) {
								e.printStackTrace();
							}


							//Operator
							String operatorName = rs.getString(8);
							String operatorValue = rs.getString(9);

							Operator opr = null;
							if (null != operatorId) {
								opr = new Operator();
								opr.setId(operatorId);
								opr.setName(operatorName);
								opr.setValue(operatorValue);
							}
							ruleLogic.setId(ruleLogicId);
							ruleLogic.setRuleId(ruleId);
							ruleLogic.setRuleSubRuleMapping(subruleMapId);
							ruleLogic.setRuleOperatorIdMapping(operatorId);
							ruleLogic.setOrderno(orderNo);
							ruleLogic.setSubRule(sr);
							ruleLogic.setOperator(opr);

							ruleLogicList.add(ruleLogic);
						}
						return ruleLogicList;
					}}, params);
							
				
				rule.setLogic(logic);
			}
		} catch (Exception e) {
			throw new DataLayerException(e);
		}
		return rule;
	}
	
	/**
	 * Fetch all rules by execution order.
	 *
	 * @return the list
	 * @throws DataLayerException the data layer exception
	 * @author  Sudhakar
	 */
	public List<Rule> fetchAllRulesBYExecutionOrder() throws DataLayerException{
		String sql=PropertyLoader.getProperty(CommonConstants.QUERY_FETCHALLRULES_SELECT);
		QueryRunner logicQr = new QueryRunner(dataSource);
		List<Rule> ruleList=null;
		try {
			ruleList = logicQr.query(sql, new ResultSetHandler<List<Rule>>() {

				@Override
				public List<Rule> handle(ResultSet rs) throws SQLException {
					List<Rule> ruleList = new ArrayList<Rule>();

					while(rs.next()){
						Rule rule = new Rule();

						rule.setId(rs.getString("RULE_ID"));
						rule.setRuleName(rs.getString("RULE_NAME"));
						rule.setRuleDescription(rs.getString("RULE_DESCRIPTION"));
						rule.setActive(rs.getBoolean("ACTIVE"));
						rule.setReturnValue(rs.getString("RETURN_VALUE"));
						rule.setExecutionOrder(rs.getInt("EXE_ORDER"));
						ruleList.add(rule);
					}

					return ruleList;
				}
			});
		} catch (Exception e) {
			log.error("", e);
			throw new DataLayerException(e);
		}

		return ruleList;
	}
	
	/**
	 * Fetch all rules by execution order.
	 *
	 * @param modelName the model name
	 * @return the list
	 * @throws DataLayerException the data layer exception
	 * @author  Sudhakar
	 */
	public List<Rule> fetchAllRulesBYExecutionOrder(String modelName) throws DataLayerException{
		List<Rule> ruleList=null;
		if (null!=modelName && !modelName.isEmpty()) {
			String sql = PropertyLoader.getProperty(CommonConstants.QUERY_FETCHALLRULES_SELECT_BYMODEL);
			QueryRunner logicQr = new QueryRunner(dataSource);
			try {
				ruleList = logicQr.query(sql, new ResultSetHandler<List<Rule>>() {

					@Override
					public List<Rule> handle(ResultSet rs) throws SQLException {
						List<Rule> ruleList = new ArrayList<Rule>();

						while (rs.next()) {
							Rule rule = new Rule();

							rule.setId(rs.getString("RULE_ID"));
							rule.setRuleName(rs.getString("RULE_NAME"));
							rule.setRuleDescription(rs.getString("RULE_DESCRIPTION"));
							rule.setActive(rs.getBoolean("ACTIVE"));
							rule.setReturnValue(rs.getString("RETURN_VALUE"));
							rule.setExecutionOrder(rs.getInt("EXE_ORDER"));
							ruleList.add(rule);
						}

						return ruleList;
					}
				}, new Object[] { modelName });
			} catch (Exception e) {
				log.error("", e);
				throw new DataLayerException(e);
			}
		}
		return ruleList;
	}
}
