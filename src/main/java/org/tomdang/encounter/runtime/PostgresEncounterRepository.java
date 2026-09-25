package org.tomdang.encounter.runtime;
import javax.sql.DataSource;
import java.sql.*;
import java.time.*;
import java.util.*;
public final class PostgresEncounterRepository implements EncounterRepository {
	private final DataSource source;
	public PostgresEncounterRepository(DataSource source) { this.source = source; }
	@Override public Collection<EncounterSession> loadOpen() {
		String sql = "SELECT * FROM tomblock.encounter_sessions WHERE state IN ('ACTIVE','SUSPENDED')";
		try (var connection=source.getConnection(); var statement=connection.prepareStatement(sql); var result=statement.executeQuery()) {
			List<EncounterSession> values=new ArrayList<>();
			while(result.next()) {
				UUID id=result.getObject("instance_id",UUID.class); Set<UUID> participants=new LinkedHashSet<>();
				try(var ps=connection.prepareStatement("SELECT player_id FROM tomblock.encounter_participants WHERE instance_id=?")){ps.setObject(1,id);try(var rows=ps.executeQuery()){while(rows.next())participants.add(rows.getObject(1,UUID.class));}}
				values.add(new EncounterSession(id,result.getString("definition_id"),result.getObject("owner_id",UUID.class),participants,
						EncounterState.valueOf(result.getString("state")),instant(result,"started_at"),instant(result,"updated_at"),instant(result,"expires_at"),nullable(result,"disconnect_deadline"),result.getLong("revision"),result.getString("failure_reason")));
			} return values;
		} catch(SQLException exception){throw new IllegalStateException("Could not load encounters",exception);}
	}
	@Override public void save(EncounterSession value) {
		String sql="INSERT INTO tomblock.encounter_sessions (instance_id,definition_id,owner_id,state,started_at,updated_at,expires_at,disconnect_deadline,revision,failure_reason) VALUES (?,?,?,?,?,?,?,?,?,?) ON CONFLICT(instance_id) DO UPDATE SET state=EXCLUDED.state,updated_at=EXCLUDED.updated_at,disconnect_deadline=EXCLUDED.disconnect_deadline,revision=EXCLUDED.revision,failure_reason=EXCLUDED.failure_reason";
		try(var connection=source.getConnection()){connection.setAutoCommit(false);try(var statement=connection.prepareStatement(sql)){statement.setObject(1,value.instanceId());statement.setString(2,value.definitionId());statement.setObject(3,value.ownerId());statement.setString(4,value.state().name());statement.setObject(5,time(value.startedAt()));statement.setObject(6,time(value.updatedAt()));statement.setObject(7,time(value.expiresAt()));statement.setObject(8,time(value.disconnectDeadline()));statement.setLong(9,value.revision());statement.setString(10,value.failureReason());statement.executeUpdate();}try(var delete=connection.prepareStatement("DELETE FROM tomblock.encounter_participants WHERE instance_id=?")){delete.setObject(1,value.instanceId());delete.executeUpdate();}try(var insert=connection.prepareStatement("INSERT INTO tomblock.encounter_participants(instance_id,player_id) VALUES (?,?)")){for(UUID player:value.participants()){insert.setObject(1,value.instanceId());insert.setObject(2,player);insert.addBatch();}insert.executeBatch();}connection.commit();}catch(SQLException exception){throw new IllegalStateException("Could not save encounter",exception);}
	}
	private static OffsetDateTime time(Instant value){return value==null?null:OffsetDateTime.ofInstant(value,ZoneOffset.UTC);}
	private static Instant instant(ResultSet result,String column)throws SQLException{return result.getObject(column,OffsetDateTime.class).toInstant();}
	private static Instant nullable(ResultSet result,String column)throws SQLException{OffsetDateTime value=result.getObject(column,OffsetDateTime.class);return value==null?null:value.toInstant();}
}
