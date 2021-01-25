const supertest = require('supertest');
const {GenericContainer} = require("testcontainers");

let dynamoContainer;
let request;

jest.setTimeout(60000)
describe('Film controller e2e tests', () => {

    let films = [{name: 'Sound of Metal', score: 7.8},
        {name: 'The Mandalorian', score: 8.8},
        {name: 'Attack on Titan', score: 8.9}]

    beforeAll(async () => {
        dynamoContainer = await new GenericContainer("amazon/dynamodb-local", "1.13.6")
            .withExposedPorts(8000)
            .start().catch((err) => {
                console.log(err)
            })


        // GET RANDOM PORT FROM CONTAINER
        process.env.DynamoDB_PORT = dynamoContainer.getMappedPort(8000);

        const AWS = require('aws-sdk');
        AWS.config.update({
            region: process.env.AWS_REGION || 'local',
            endpoint: process.env.AWS_DYNAMO_ENDPOINT || `http://localhost:${process.env.DynamoDB_PORT}`,
            accessKeyId: "xxxxxx", // No es necesario poner nada aquí
            secretAccessKey: "xxxxxx" // No es necesario poner nada aquí
        });


        // INIT DATABASE
        const createTableIfNotExist = require("../../src/db/createTable");
        await createTableIfNotExist('films');

        // INIT APP (CREATE MIGRATIONS)
        const app = require('../../src/app');
        request = supertest(app);

        // RUN MIGRATIONS
        for (const film of films) {
            await request.post('/api/films/').send(film).expect(201);
        }

    });

    afterAll(async () => {
        await dynamoContainer.stop();
    });

    test('Get all the movies', async () => {

        const response = await request.get('/api/films').expect(200);

        expect(response.body).toHaveLength(3);

        expect(response.body).toEqual(
            expect.arrayContaining([
                expect.objectContaining(films[0]),
                expect.objectContaining(films[1]),
                expect.objectContaining(films[2])
            ])
        )
    })

    test('Insert a movie', async () => {
        let film = films[0];
        film.id = films.length;
        const response = await request.post('/api/films/')
            .send(film)
            .expect(201)

        expect(response.body.id).toBe(film.id);
        expect(response.body.name).toBe(film.name);
        expect(response.body.score).toBe(film.score);
    })

});